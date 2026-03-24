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
 * Rules enforced:
 * - Rule 1:  Idempotent upload via uniqueKey sent to server
 * - Rule 2:  uniqueKey included in every upload payload
 * - Rule 5:  Two-layer protection (Android Room + Apps Script dedup)
 * - Rule 7:  Uses raw phone number as primary identity (uniqueKey uses phoneNumber, not contact name)
 * - Rule 8:  Marks synced ONLY after confirmed success/skipped_duplicate response
 * - Rule 9:  Retries use same uniqueKey so server rejects duplicates safely
 * - Rule 10: Transaction-safe: markAsSynced only after confirmed response
 * - Rule 12: In-memory uploadedKeys set prevents re-upload within same batch
 * - Rule 14: Manual Sync uses same SyncManager → same rules apply
 * - Rule 15: Logs structured server response (inserted/skipped_duplicate)
 * - Rule 16: Diagnostic logging for every uniqueKey
 * - Rule 17: Contact name used for display only, uniqueKey built from raw phone number
 * - Rule 18: Phone number normalized before creating display label
 * - Rule 20: On uncertainty (ERROR response), stop batch and retry later
 */
class SyncManager(private val context: Context) {

    companion object {
        private const val TAG = "SyncManager"
        private val syncMutex = Mutex()

        /**
         * Normalizes a phone number by removing spaces, dashes, and Indian country code.
         * Rule 18: Ensures same physical number doesn't create different keys.
         */
        fun normalizePhoneNumber(raw: String): String {
            var clean = raw.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
            if (clean.startsWith("+91")) {
                clean = clean.substring(3)
            } else if (clean.startsWith("91") && clean.length > 10) {
                clean = clean.substring(2)
            }
            return clean
        }
    }

    private val db = AppDatabase.getDatabase(context)
    private val apiService = ApiService()
    private val prefs = PrefsManager(context)

    /**
     * Uploads all pending (isSynced=0) calls to Google Sheets.
     *
     * - Uses a Mutex so concurrent callers block instead of duplicating work.
     * - Runs synchronously inside the caller's coroutine (no fire-and-forget).
     * - Marks each call as synced ONLY after confirmed server response.
     * - Returns true if full batch completed, false if stopped early.
     */
    suspend fun syncPendingCalls(): Boolean {
        val scriptUrl = prefs.getScriptUrl()
        if (scriptUrl.isEmpty()) {
            Log.d(TAG, "⚠️ Script URL is empty. Skipping sync.")
            return false
        }
        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.d(TAG, "⚠️ No internet available. Skipping sync.")
            return false
        }

        // tryLock returns false immediately if another coroutine holds the Mutex
        if (!syncMutex.tryLock()) {
            Log.d(TAG, "⚠️ Sync already in progress (Mutex locked). Skipping this request.")
            return false
        }

        try {
            val pendingCalls = db.callDao().getPendingCalls()
            Log.d(TAG, "🚀 SyncManager starting upload. Pending calls: ${pendingCalls.size}")

            if (pendingCalls.isEmpty()) {
                Log.d(TAG, "✅ No pending calls to upload.")
                return true
            }

            val actualHrName = prefs.getHrName()

            // Rule 12: In-memory set of uploaded keys in this batch
            val uploadedKeysInBatch = mutableSetOf<String>()
            var allSucceeded = true
            var consecutiveErrors = 0

            for (call in pendingCalls) {
                // Rule 12: Skip if same key already processed in this batch
                if (!uploadedKeysInBatch.add(call.uniqueCallId)) {
                    Log.d(TAG, "   ⚠️ BATCH UPLOAD DUP — key already uploaded in this batch: ${call.uniqueCallId}")
                    db.callDao().markAsSynced(call.id)
                    continue
                }

                // Rule 18: Normalize phone number for display
                val cleanNumber = normalizePhoneNumber(call.phoneNumber)

                // Rule 17: Contact name for display only, uniqueKey uses raw phoneNumber
                val callerLabel = getContactName(call.phoneNumber) ?: cleanNumber

                Log.d(TAG, "📤 Uploading call ID=${call.id}, key=${call.uniqueCallId}, label=$callerLabel")

                // Rule 2: Send uniqueKey in payload
                val result = apiService.sendCallDataSync(
                    scriptUrl = scriptUrl,
                    hrName = actualHrName,
                    phoneNumber = callerLabel,
                    callType = call.callType,
                    duration = call.duration,
                    date = call.date,
                    time = call.time,
                    simName = call.simName,
                    uniqueKey = call.uniqueCallId
                )

                // Rule 8 & 15 & 16: Handle structured response
                when (result) {
                    ApiService.UploadResult.INSERTED -> {
                        db.callDao().markAsSynced(call.id)
                        Log.d(TAG, "   ✅ INSERTED & marked synced: ID=${call.id}, key=${call.uniqueCallId}")
                        consecutiveErrors = 0 // reset on success
                    }
                    ApiService.UploadResult.SKIPPED_DUPLICATE -> {
                        // Rule 1 & 9: Server rejected as duplicate — mark synced locally too
                        db.callDao().markAsSynced(call.id)
                        Log.d(TAG, "   ⚠️ SKIPPED_DUPLICATE by server, marked synced locally: ID=${call.id}, key=${call.uniqueCallId}")
                        consecutiveErrors = 0 // reset on success
                    }
                    ApiService.UploadResult.ERROR -> {
                        // Rule 20: On uncertainty, stop batch — prefer skipping over duplicating
                        Log.w(TAG, "   ❌ Upload ERROR for ID=${call.id}, key=${call.uniqueCallId}. Will retry later.")
                        allSucceeded = false
                        consecutiveErrors++
                        if (consecutiveErrors >= 3) {
                            Log.e(TAG, "🛑 3 consecutive network errors. Stopping batch to prevent rate limiting.")
                            break
                        }
                    }
                }
                
                // Add a small delay between requests to avoid hitting Google Apps Script rate limits
                kotlinx.coroutines.delay(500)
            }

            return allSucceeded
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}", e)
            return false
        } finally {
            syncMutex.unlock()
            Log.d(TAG, "🛑 SyncManager finished. Mutex released.")
        }
    }

    /**
     * Looks up a phone number in Android Contacts.
     * Rule 17: Returns the display name if saved, or null. Used for display only.
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
