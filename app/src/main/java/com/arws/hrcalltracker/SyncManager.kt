package com.arws.hrcalltracker

import android.content.Context
import android.util.Log
import com.arws.hrcalltracker.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SyncManager — Handles sending pending calls from local SQLite database to Google Sheets.
 *
 * It pulls all pending calls from the database. If internet is available,
 * it tries to send them one by one. If successful, it deletes the call from the DB.
 */
class SyncManager(private val context: Context) {

    companion object {
        private const val TAG = "SyncManager"
    }

    private val db = AppDatabase.getDatabase(context)
    private val apiService = ApiService()
    private val prefs = PrefsManager(context)

    /**
     * Trigger a sync of all pending calls stored locally.
     * Runs on the IO coroutine dispatcher safely in the background.
     */
    fun syncPendingCalls() {
        val scriptUrl = prefs.getScriptUrl()
        val hrName = prefs.getHrName()
        val empId = prefs.getEmployeeId()

        if (scriptUrl.isEmpty()) {
            Log.e(TAG, "⚠️ No Script URL configured. Sync deferred.")
            return
        }

        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.d(TAG, "📶 No internet connection. Sync deferred. Calls are safe locally.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pendingCalls = db.callDao().getPendingCalls()

                if (pendingCalls.isEmpty()) {
                    Log.d(TAG, "✅ No pending calls to sync.")
                    return@launch
                }

                Log.d(TAG, "🔄 Found ${pendingCalls.size} pending calls. Starting sync...")

                for (call in pendingCalls) {
                    val success = apiService.sendCallDataSync(
                        scriptUrl = scriptUrl,
                        hrName = hrName,
                        employeeId = empId,
                        phoneNumber = call.phoneNumber,
                        callType = call.callType,
                        duration = call.duration,
                        date = call.date,
                        simName = call.simName
                    )

                    if (success) {
                        // Data successfully sent -> delete from local DB
                        db.callDao().deleteCall(call.id)
                        Log.d(TAG, "✅ Call ID ${call.id} synced and removed from database.")
                    } else {
                        Log.e(TAG, "❌ Failed to sync Call ID ${call.id}. Will retry later.")
                        // If one fails, we might still be having network issues. Break out to retry later.
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync: ${e.message}", e)
            }
        }
    }
}
