package com.arws.hrcalltracker

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.arws.hrcalltracker.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SyncManager — Handles sending pending calls from local SQLite database to Google Sheets.
 *
 * For each call it will:
 *  - Look up the phone number in Android Contacts
 *  - If found → send the contact's display name
 *  - If NOT found → send the raw phone number
 */
class SyncManager(private val context: Context) {

    companion object {
        private const val TAG = "SyncManager"
        @Volatile
        private var isSyncing = false
    }

    private val db = AppDatabase.getDatabase(context)
    private val apiService = ApiService()
    private val prefs = PrefsManager(context)

    fun syncPendingCalls() {
        if (isSyncing) {
            Log.d(TAG, "⚠️ Sync already in progress (another thread/worker is uploading). Skipping.")
            return
        }

        val scriptUrl = prefs.getScriptUrl()

        if (scriptUrl.isEmpty()) return
        if (!NetworkUtils.isInternetAvailable(context)) return

        isSyncing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pendingCalls = db.callDao().getPendingCalls()
                Log.d(TAG, "🚀 SyncManager starting API upload loop. Total pending calls queued for upload: ${pendingCalls.size}")
                val actualHrName = prefs.getHrName()

                for (call in pendingCalls) {
                    // Remove Indian country code (+91 or 91) and any spaces/dashes
                    var cleanNumber = call.phoneNumber.replace(" ", "").replace("-", "")
                    if (cleanNumber.startsWith("+91")) {
                        cleanNumber = cleanNumber.substring(3)
                    } else if (cleanNumber.startsWith("91") && cleanNumber.length > 10) {
                        cleanNumber = cleanNumber.substring(2)
                    }

                    // Look up contact name — if saved use name, else use the formatted phone number
                    val callerLabel = getContactName(call.phoneNumber) ?: cleanNumber

                    val success = apiService.sendCallDataSync(
                        scriptUrl = scriptUrl,
                        hrName = actualHrName,          // Always the HR's name
                        phoneNumber = callerLabel,      // Contact Name OR 10-digit number
                        callType = call.callType,
                        duration = call.duration,
                        date = call.date,
                        time = call.time,
                        simName = call.simName
                    )

                    if (success) {
                        db.callDao().markAsSynced(call.id)
                        Log.d(TAG, "✅ Synced and marked as done call ID=${call.id} (${call.phoneNumber})")
                    } else {
                        Log.w(TAG, "❌ Sync failed for call ID=${call.id}. Will retry later.")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync: ${e.message}")
            } finally {
                isSyncing = false
                Log.d(TAG, "🛑 SyncManager finished. Lock released.")
            }
        }
    }

    /**
     * Looks up a phone number in Android Contacts.
     * Returns the display name if the number is saved, or null if not found.
     */
    private fun getContactName(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null
        var cursor: Cursor? = null
        return try {
            val uri = android.net.Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(phoneNumber)
            )
            cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact for $phoneNumber: ${e.message}")
            null
        } finally {
            cursor?.close()
        }
    }
}
