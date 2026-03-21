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

        val lastScanTime = prefs.getLastUploadedTimestamp()
        val scanSince = if (lastScanTime == 0L) {
            System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        } else {
            lastScanTime
        }

        val newCalls = callLogHelper.scanCallsSince(scanSince)
        val companySimId = prefs.getCompanySimId()
        val companySimName = prefs.getCompanySimName()
        var maxScannedTimestamp = lastScanTime

        for (call in newCalls) {
            if (call.dateMillis > maxScannedTimestamp) {
                maxScannedTimestamp = call.dateMillis
            }

            if (call.subscriptionId == companySimId) {
                val uniqueCallId = "${call.phoneNumber}_${call.dateMillis}_${call.duration}"
                val exists = db.callDao().checkExists(uniqueCallId)
                if (exists == 0) {
                    val entity = CallEntity(
                        phoneNumber = call.phoneNumber,
                        callType = call.callType,
                        duration = call.duration,
                        date = call.date,
                        time = call.time,
                        simName = companySimName,
                        uniqueCallId = uniqueCallId
                    )
                    db.callDao().insertCall(entity)
                }
            }
        }

        prefs.saveLastUploadedTimestamp(maxScannedTimestamp)
        
        val syncManager = SyncManager(context)
        syncManager.syncPendingCalls()

        return@withContext Result.success()
    }
}
