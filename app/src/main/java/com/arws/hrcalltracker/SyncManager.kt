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

        if (scriptUrl.isEmpty()) return
        if (!NetworkUtils.isInternetAvailable(context)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pendingCalls = db.callDao().getPendingCalls()

                for (call in pendingCalls) {
                    val success = apiService.sendCallDataSync(
                        scriptUrl = scriptUrl,
                        hrName = hrName,
                        phoneNumber = call.phoneNumber,
                        callType = call.callType,
                        duration = call.duration,
                        date = call.date,
                        time = call.time,
                        simName = call.simName
                    )

                    if (success) {
                        db.callDao().deleteCall(call.id)
                    } else {
                        break 
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync: ${e.message}")
            }
        }
    }
}
