package com.arws.hrcalltracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arws.hrcalltracker.db.AppDatabase
import com.arws.hrcalltracker.db.CallEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Periodic Sync Worker
 */
class PeriodicSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "PeriodicSyncWorker"
        const val WORK_NAME = "PeriodicCallLogSync"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = PrefsManager(context)
        val callLogHelper = CallLogHelper(context)
        val db = AppDatabase.getDatabase(context)

        if (!prefs.isSetupComplete() || prefs.getScriptUrl().isEmpty()) {
            return@withContext Result.failure()
        }

        // Clean up old synced calls (older than 7 days) to prevent infinite DB growth
        val sevenDaysAgoMillis = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        db.callDao().deleteOldSyncedCalls(sevenDaysAgoMillis)

        val lastScanTime = prefs.getLastUploadedTimestamp()
        // If never scanned, go back 24 hours. Otherwise start from last scanned time.
        val scanSince = if (lastScanTime == 0L) {
            System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        } else {
            lastScanTime
        }

        Log.d(TAG, "🔍 Starting Periodic Scan. current last_scan_timestamp: $lastScanTime (scanning since: $scanSince)")

        // 1. Properly queries CallLog.Calls for only records newer than scanSince.
        // 2. Uses `cursor.moveToNext()` internally to properly loop through all matching rows.
        // 3. Extracts values correctly into fresh CallInfo objects for each row natively.
        val newCalls = callLogHelper.scanCallsSince(scanSince)
        Log.d(TAG, "🔍 Total call log rows fetched natively: ${newCalls.size}")

        val companySimId = prefs.getCompanySimId()
        val companySimName = prefs.getCompanySimName()
        var maxScannedTimestamp = lastScanTime

        // Iterates through EVERY matching row correctly
        for (call in newCalls) {
            Log.d(TAG, "📞 Evaluating row: Number: ${call.phoneNumber}, Date: ${call.dateMillis}, Duration: ${call.duration}, Type: ${call.callType}, SIM: ${call.subscriptionId}")

            // Always advance the scan window to avoid re-reading old calls later
            if (call.dateMillis > maxScannedTimestamp) {
                maxScannedTimestamp = call.dateMillis
            }

            // Filter by selected SIM
            if (call.subscriptionId == companySimId) {
                // Generate a unique key for each call using: phone number + date + duration
                val uniqueCallId = "${call.phoneNumber}_${call.dateMillis}_${call.duration}"
                val exists = db.callDao().checkExists(uniqueCallId)
                
                if (exists == 0) {
                    // Create a fresh call model entity object for SQLite
                    val entity = CallEntity(
                        phoneNumber = call.phoneNumber,
                        callType = call.callType,
                        duration = call.duration,
                        date = call.date,
                        time = call.time,
                        simName = companySimName,
                        dateMillis = call.dateMillis,
                        uniqueCallId = uniqueCallId
                    )
                    db.callDao().insertCall(entity)
                    Log.d(TAG, "   ✅ Row queued for upload: $uniqueCallId")
                } else {
                    Log.d(TAG, "   ⚠️ Row skipped as duplicate (already in Room DB): $uniqueCallId")
                }
            } else {
                Log.d(TAG, "   ❌ Row ignored due to SIM mismatch. (Row SIM: ${call.subscriptionId}, Required: $companySimId)")
            }
        }

        // Save the maximum timestamp we scanned up to, so next scan is fresh
        if (maxScannedTimestamp > lastScanTime) {
            prefs.saveLastUploadedTimestamp(maxScannedTimestamp)
            Log.d(TAG, "🆙 Final updated last_scan_timestamp to: $maxScannedTimestamp")
        } else {
            Log.d(TAG, "⏭️ last_scan_timestamp remains unchanged: $lastScanTime")
        }

        // Sync any pending (not-yet-uploaded) calls to Google Sheets
        val syncManager = SyncManager(context)
        syncManager.syncPendingCalls()

        return@withContext Result.success()
    }
}
