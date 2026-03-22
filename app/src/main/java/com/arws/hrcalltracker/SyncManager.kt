package com.arws.hrcalltracker

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.arws.hrcalltracker.db.AppDatabase
import kotlinx.coroutines.sync.Mutex

/**
 * SyncManager — Handles sending pending calls from local Room DB to Google Sheets.
 *
 * CRITICAL FIX: This is now a suspend function (not fire-and-forget).
 * It uses a Mutex to guarantee only one upload cycle runs at a time,
 * preventing the race condition that caused duplicate rows in Sheets.
 */
class SyncManager(private val context: Context) {

    companion object {
        private const val TAG = "SyncManager"
        private val syncMutex = Mutex()
    }

    private val db = AppDatabase.getDatabase(context)
    private val apiService = ApiService()
    private val prefs = PrefsManager(context)

    /**
     * Uploads all pending (isSynced=0) calls to Google Sheets.
     *
     * - Uses a Mutex so concurrent callers block instead of duplicating work.
     * - Runs synchronously inside the caller's coroutine (no fire-and-forget).
     * - Marks each call as synced immediately after successful upload.
     */
    suspend fun syncPendingCalls() {
        val scriptUrl = prefs.getScriptUrl()
        if (scriptUrl.isEmpty()) {
            Log.d(TAG, "⚠️ Script URL is empty. Skipping sync.")
            return
        }
        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.d(TAG, "⚠️ No internet available. Skipping sync.")
            return
        }

        // tryLock returns false immediately if another coroutine holds the Mutex
        if (!syncMutex.tryLock()) {
            Log.d(TAG, "⚠️ Sync already in progress (Mutex locked). Skipping this request.")
            return
        }

        try {
            val pendingCalls = db.callDao().getPendingCalls()
            Log.d(TAG, "🚀 SyncManager starting upload. Pending calls: ${pendingCalls.size}")

            if (pendingCalls.isEmpty()) {
                Log.d(TAG, "✅ No pending calls to upload.")
                return
            }

            val actualHrName = prefs.getHrName()

            for (call in pendingCalls) {
                // Clean Indian country code
                var cleanNumber = call.phoneNumber.replace(" ", "").replace("-", "")
                if (cleanNumber.startsWith("+91")) {
                    cleanNumber = cleanNumber.substring(3)
                } else if (cleanNumber.startsWith("91") && cleanNumber.length > 10) {
                    cleanNumber = cleanNumber.substring(2)
                }

                // Look up contact name
                val callerLabel = getContactName(call.phoneNumber) ?: cleanNumber

                Log.d(TAG, "📤 Uploading call ID=${call.id}, key=${call.uniqueCallId}, label=$callerLabel")

                val success = apiService.sendCallDataSync(
                    scriptUrl = scriptUrl,
                    hrName = actualHrName,
                    phoneNumber = callerLabel,
                    callType = call.callType,
                    duration = call.duration,
                    date = call.date,
                    time = call.time,
                    simName = call.simName
                )

                if (success) {
                    db.callDao().markAsSynced(call.id)
                    Log.d(TAG, "   ✅ Uploaded & marked synced: ID=${call.id}, key=${call.uniqueCallId}")
                } else {
                    Log.w(TAG, "   ❌ Upload failed for ID=${call.id}. Stopping batch to retry later.")
                    break  // Stop on first failure to preserve order
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}", e)
        } finally {
            syncMutex.unlock()
            Log.d(TAG, "🛑 SyncManager finished. Mutex released.")
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
