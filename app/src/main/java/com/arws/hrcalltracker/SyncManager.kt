package com.arws.hrcalltracker

import android.content.Context
import android.util.Log
import com.arws.hrcalltracker.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SyncManager — Handles sending pending calls from local SQLite database to Google Sheets.
 */
class SyncManager(private val context: Context) {

    companion object {
        private const val TAG = "SyncManager"
    }

    private val db = AppDatabase.getDatabase(context)
    private val apiService = ApiService()
    private val prefs = PrefsManager(context)

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
                    Log.d(TAG, "📡 Sending data for ${call.phoneNumber} to GAS...")
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
                        db.callDao().deleteCall(call.id)
                        Log.d(TAG, "✅ Sync Success: ${call.phoneNumber}")
                    } else {
                        Log.e(TAG, "❌ Sync Failed for ${call.phoneNumber}. Response was false.")
                        break // Stop and retry later
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync: ${e.message}", e)
            }
        }
    }
}
