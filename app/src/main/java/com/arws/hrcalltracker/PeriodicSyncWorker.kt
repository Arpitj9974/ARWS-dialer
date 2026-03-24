package com.arws.hrcalltracker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arws.hrcalltracker.db.AppDatabase
import com.arws.hrcalltracker.db.CallEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Periodic Sync Worker — Clock-aligned, boundary-based call log processing.
 *
 * 21-RULE ANTI-DUPLICATE DESIGN:
 * - Rule 6:  Uses exact dateMillis (millisecond precision) in uniqueKey
 * - Rule 7:  Uses raw phone number, not contact name
 * - Rule 10: Boundary saved ONLY after full batch completion
 * - Rule 11: Ghost/invalid rows skipped before processing
 * - Rule 12: In-memory batch-level dedup set
 * - Rule 13: Single worker via WorkManager unique name + sync lock
 * - Rule 14: Manual sync uses same worker → same rules
 * - Rule 18: Phone number normalized before uniqueKey creation
 * - Rule 19: Boundary persisted ONLY after full batch is safely handled
 * - Rule 20: If uncertain, prefer skip over duplicate
 */
class PeriodicSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "PeriodicSyncWorker"
        const val WORK_NAME = "PeriodicCallLogSync"

        /**
         * Computes the current half-hour boundary (floor).
         * E.g. if now is 12:17 → returns epoch for 12:00
         *      if now is 12:42 → returns epoch for 12:30
         */
        fun getCurrentBoundary(nowMillis: Long): Long {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val minute = cal.get(Calendar.MINUTE)
            cal.set(Calendar.MINUTE, if (minute < 30) 0 else 30)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        /**
         * Computes delay in milliseconds until the next :00 or :30 boundary.
         */
        fun getDelayToNextBoundary(nowMillis: Long): Long {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val minute = cal.get(Calendar.MINUTE)
            val nextBoundaryMinute = if (minute < 30) 30 else 60
            val delayMinutes = nextBoundaryMinute - minute
            val delayMs = delayMinutes * 60 * 1000L -
                    cal.get(Calendar.SECOND) * 1000L -
                    cal.get(Calendar.MILLISECOND)
            return if (delayMs <= 0) 30 * 60 * 1000L else delayMs
        }

        private val logDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        fun formatLogTime(millis: Long): String = logDateFormat.format(Date(millis))
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = PrefsManager(context)
        val callLogHelper = CallLogHelper(context)
        val db = AppDatabase.getDatabase(context)

        if (!prefs.isSetupComplete() || prefs.getScriptUrl().isEmpty()) {
            Log.d(TAG, "⚠️ Setup not complete or script URL empty. Skipping.")
            return@withContext Result.failure()
        }

        // ── Rule 13: Sync lock check — prevent parallel workers ──
        if (prefs.isSyncRunning()) {
            Log.d(TAG, "⚠️ SYNC SKIPPED — another sync is still flagged as running.")
            return@withContext Result.retry()
        }
        prefs.setSyncRunning(true)

        try {
            // Clean up old synced calls (older than 7 days)
            val sevenDaysAgoMillis = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            db.callDao().deleteOldSyncedCalls(sevenDaysAgoMillis)

            val nowMillis = System.currentTimeMillis()
            val currentBoundary = getCurrentBoundary(nowMillis)
            val previousBoundary = prefs.getLastProcessedBoundary()

            Log.d(TAG, "═══════════════════════════════════════════")
            Log.d(TAG, "🕐 SYNC START")
            Log.d(TAG, "   Current mobile time:         ${formatLogTime(nowMillis)}")
            Log.d(TAG, "   Previous processed boundary: ${formatLogTime(previousBoundary)} ($previousBoundary)")
            Log.d(TAG, "   Current target boundary:     ${formatLogTime(currentBoundary)} ($currentBoundary)")

            // If we've already processed this boundary, skip scanning for new calls
            if (previousBoundary >= currentBoundary) {
                Log.d(TAG, "⏭️ Already processed up to this boundary. Scanning skipped.")
                
                // CRITICAL FIX: Even if no new calls to scan, we MUST try to upload 
                // any lingering pending calls that failed due to network errors previously!
                val syncManager = SyncManager(context)
                val uploadSuccess = syncManager.syncPendingCalls()
                
                if (uploadSuccess) {
                    Log.d(TAG, "🆙 ✅ Retried pending batch succeeded.")
                    return@withContext Result.success()
                } else {
                    Log.w(TAG, "🆙 ⚠️ Retried pending batch still failing. Will retry later.")
                    return@withContext Result.retry()
                }
            }

            // Determine scan start: use previousBoundary if set, else go back 24h
            val scanStart = if (previousBoundary == 0L) {
                nowMillis - 24 * 60 * 60 * 1000L
            } else {
                previousBoundary
            }

            Log.d(TAG, "🔍 Call log query: DATE > ${formatLogTime(scanStart)} AND DATE <= ${formatLogTime(currentBoundary)}")

            // Step 1: Query call log for rows in (scanStart, currentBoundary]
            val newCalls = callLogHelper.scanCallsInRange(scanStart, currentBoundary)
            Log.d(TAG, "🔍 Total rows fetched from call log: ${newCalls.size}")

            val companySimId = prefs.getCompanySimId()
            val companySimName = prefs.getCompanySimName()

            // Step 2: Build unique keys and deduplicate within batch
            // Rule 12: In-memory set for cooldown protection
            val seenKeysInBatch = mutableSetOf<String>()
            var insertedCount = 0
            var skippedBatchDup = 0
            var skippedRoomDup = 0
            var skippedSimMismatch = 0
            var skippedGhostRow = 0

            for (call in newCalls) {
                // ── Rule 11: Skip ghost/invalid rows ──
                if (call.phoneNumber.isBlank() || call.phoneNumber == "Unknown" ||
                    call.dateMillis <= 0 || call.callType.isBlank() ||
                    call.subscriptionId < 0) {
                    skippedGhostRow++
                    Log.d(TAG, "   👻 GHOST ROW SKIPPED: num='${call.phoneNumber}', dateMs=${call.dateMillis}, type='${call.callType}', sim=${call.subscriptionId}")
                    continue
                }

                // Filter by selected SIM
                if (call.subscriptionId != companySimId) {
                    skippedSimMismatch++
                    Log.d(TAG, "   ❌ SIM mismatch: ${call.phoneNumber} (row SIM=${call.subscriptionId}, required=$companySimId)")
                    continue
                }

                // ── Rule 18: Normalize phone number before uniqueKey ──
                val normalizedNumber = SyncManager.normalizePhoneNumber(call.phoneNumber)

                // ── Rule 6 & 7: Unique key using exact dateMillis + raw normalized number ──
                val uniqueCallId = "${normalizedNumber}_${call.dateMillis}_${call.duration}_${call.callType}_${call.subscriptionId}"

                Log.d(TAG, "📞 Row: num=${call.phoneNumber}, normalized=$normalizedNumber, date=${call.date}, time=${call.time}, dur=${call.duration}, type=${call.callType}, sim=${call.subscriptionId}")
                Log.d(TAG, "   🔑 uniqueKey=$uniqueCallId")

                // ── Rule 12: Batch-level dedup ──
                if (!seenKeysInBatch.add(uniqueCallId)) {
                    skippedBatchDup++
                    Log.d(TAG, "   ⚠️ BATCH DUPLICATE — same key already seen in this scan. Skipped.")
                    continue
                }

                // Room-level dedup: check if already exists in database
                val exists = db.callDao().checkExists(uniqueCallId)
                if (exists > 0) {
                    skippedRoomDup++
                    Log.d(TAG, "   ⚠️ ROOM DUPLICATE — key already exists in database. Skipped.")
                    continue
                }

                // Insert as pending
                val entity = CallEntity(
                    phoneNumber = call.phoneNumber,  // Store raw for contact lookup later
                    contactName = call.contactName,
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
                        Log.d(TAG, "   ✅ INSERTED as pending (rowId=$rowId)")
                    } else {
                        skippedRoomDup++
                        Log.d(TAG, "   ⚠️ INSERT IGNORED by Room unique constraint")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "   ❌ INSERT ERROR: $uniqueCallId — ${e.message}")
                }
            }

            Log.d(TAG, "📊 SCAN SUMMARY: fetched=${newCalls.size}, inserted=$insertedCount, batchDup=$skippedBatchDup, roomDup=$skippedRoomDup, simMismatch=$skippedSimMismatch, ghostRows=$skippedGhostRow")

            // Step 3: Upload pending calls (synchronous — awaits completion)
            val syncManager = SyncManager(context)
            val uploadSuccess = syncManager.syncPendingCalls()

            // ── Rule 19: Persist boundary ONLY after full batch completion ──
            if (uploadSuccess) {
                prefs.saveLastProcessedBoundary(currentBoundary)
                prefs.saveLastUploadedTimestamp(nowMillis)
                Log.d(TAG, "🆙 ✅ Full batch succeeded. Saved processed boundary: ${formatLogTime(currentBoundary)} ($currentBoundary)")
            } else {
                Log.w(TAG, "🆙 ⚠️ Batch had errors. Boundary NOT updated (will retry unprocessed calls next sync).")
            }

            Log.d(TAG, "═══════════════════════════════════════════")

            return@withContext Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ SYNC ERROR: ${e.message}", e)
            return@withContext Result.retry()
        } finally {
            prefs.setSyncRunning(false)
        }
    }
}
