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
 * Periodically wakes up, scans the call log for new calls,
 * saves matching calls to the local Room database, and triggers a sync.
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
        Log.d(TAG, "Starting periodic call log scan...")
        
        val prefs = PrefsManager(context)
        val callLogHelper = CallLogHelper(context)
        val db = AppDatabase.getDatabase(context)

        // 1. Check if setup is complete and script URL is available
        if (!prefs.isSetupComplete() || prefs.getScriptUrl().isEmpty()) {
            Log.w(TAG, "Setup incomplete or Script URL missing. Aborting sync.")
            return@withContext Result.failure()
        }

        // 2. Get last scan timestamp (default to 1 day ago)
        val lastScanTime = prefs.getLastUploadedTimestamp()
        val scanSince = if (lastScanTime == 0L) {
            System.currentTimeMillis() - 24 * 60 * 60 * 1000L // 1 day ago
        } else {
            lastScanTime
        }

        // 3. Scan the new calls
        val newCalls = callLogHelper.scanCallsSince(scanSince)
        Log.d(TAG, "Found ${newCalls.size} new calls to process since $scanSince")

        val companySimId = prefs.getCompanySimId()
        val companySimName = prefs.getCompanySimName()
        var maxScannedTimestamp = lastScanTime

        // 4. Process and filter calls
        for (call in newCalls) {
            // Update max timestamp
            if (call.date > maxScannedTimestamp) {
                maxScannedTimestamp = call.date
            }

            // SIM Filtering
            if (call.subscriptionId == companySimId) {
                Log.d(TAG, "Matching Company SIM Call: ${call.phoneNumber} (${call.duration} sec)")

                // Unique ID: number_date_duration
                val uniqueCallId = "${call.phoneNumber}_${call.date}_${call.duration}"

                // Check for duplicates
                val exists = db.callDao().checkExists(uniqueCallId)
                if (exists == 0) {
                    val entity = CallEntity(
                        hrName = prefs.getHrName(),
                        phoneNumber = call.phoneNumber,
                        callType = call.callType,
                        duration = call.duration,
                        date = call.formattedDate,
                        simName = companySimName,
                        uniqueCallId = uniqueCallId
                    )
                    val insertedId = db.callDao().insertCall(entity)
                    Log.d(TAG, "Saved matching call to DB (Row $insertedId)")
                } else {
                    Log.d(TAG, "Call already exists in DB, skipping.")
                }
            } else {
                Log.d(TAG, "Ignored call from different SIM (${call.subscriptionId})")
            }
        }

        // 5. Update last scan time
        prefs.saveLastUploadedTimestamp(maxScannedTimestamp)
        
        // 6. Trigger a sync to Google Apps Script
        val syncManager = SyncManager(context)
        syncManager.syncPendingCalls()

        Log.d(TAG, "Periodic scan completed.")
        return@withContext Result.success()
    }
}
