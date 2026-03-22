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
 * Periodic Sync Worker — Scans call log, deduplicates, inserts into Room, then uploads.
 *
 * DUPLICATE FIX:
 * 1. Batch-level dedup: filters duplicate uniqueCallIds within a single scan result.
 * 2. Room-level dedup: checks DB before insert + unique index with IGNORE strategy.
 * 3. Synchronous upload: awaits SyncManager (no fire-and-forget).
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

        Log.d(TAG, "═══════════════════════════════════════════")
        Log.d(TAG, "🔍 SCAN START. last_scan_timestamp=$lastScanTime, scanning since=$scanSince")

        // Step 1: Query call log for rows >= scanSince
        val newCalls = callLogHelper.scanCallsSince(scanSince)
        Log.d(TAG, "🔍 Total rows fetched from call log: ${newCalls.size}")

        val companySimId = prefs.getCompanySimId()
        val companySimName = prefs.getCompanySimName()
        var maxScannedTimestamp = lastScanTime

        // Step 2: Build unique keys and deduplicate within this batch
        val seenKeysInBatch = mutableSetOf<String>()
        var insertedCount = 0
        var skippedBatchDup = 0
        var skippedRoomDup = 0
        var skippedSimMismatch = 0

        for (call in newCalls) {
            // Always advance the scan window
            if (call.dateMillis > maxScannedTimestamp) {
                maxScannedTimestamp = call.dateMillis
            }

            // Filter by selected SIM
            if (call.subscriptionId != companySimId) {
                skippedSimMismatch++
                Log.d(TAG, "   ❌ SIM mismatch: ${call.phoneNumber} (row SIM=${call.subscriptionId}, required=$companySimId)")
                continue
            }

            // Generate unique key
            val uniqueCallId = "${call.phoneNumber}_${call.dateMillis}_${call.duration}_${call.callType}_${call.subscriptionId}"

            Log.d(TAG, "📞 Row: num=${call.phoneNumber}, date=${call.date}, time=${call.time}, dur=${call.duration}, type=${call.callType}, sim=${call.subscriptionId}, key=$uniqueCallId")

            // Batch-level dedup: skip if we already processed this exact key in this scan
            if (!seenKeysInBatch.add(uniqueCallId)) {
                skippedBatchDup++
                Log.d(TAG, "   ⚠️ BATCH DUPLICATE — same key already seen in this scan. Skipped.")
                continue
            }

            // Room-level dedup: skip if already in database
            val exists = db.callDao().checkExists(uniqueCallId)
            if (exists > 0) {
                skippedRoomDup++
                Log.d(TAG, "   ⚠️ ROOM DUPLICATE — key already exists in database. Skipped.")
                continue
            }

            // Insert as pending
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
            try {
                val rowId = db.callDao().insertCall(entity)
                if (rowId != -1L) {
                    insertedCount++
                    Log.d(TAG, "   ✅ INSERTED as pending (rowId=$rowId): $uniqueCallId")
                } else {
                    skippedRoomDup++
                    Log.d(TAG, "   ⚠️ INSERT IGNORED by Room unique constraint: $uniqueCallId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "   ❌ INSERT ERROR: $uniqueCallId — ${e.message}")
            }
        }

        // Step 3: Update scan timestamp
        if (maxScannedTimestamp > lastScanTime) {
            prefs.saveLastUploadedTimestamp(maxScannedTimestamp)
            Log.d(TAG, "🆙 Updated last_scan_timestamp: $lastScanTime → $maxScannedTimestamp")
        } else {
            Log.d(TAG, "⏭️ last_scan_timestamp unchanged: $lastScanTime")
        }

        Log.d(TAG, "📊 SCAN SUMMARY: fetched=${newCalls.size}, inserted=$insertedCount, batchDup=$skippedBatchDup, roomDup=$skippedRoomDup, simMismatch=$skippedSimMismatch")

        // Step 4: Upload pending calls (synchronous — awaits completion)
        val syncManager = SyncManager(context)
        syncManager.syncPendingCalls()

        Log.d(TAG, "═══════════════════════════════════════════")

        return@withContext Result.success()
    }
}
