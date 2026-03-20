package com.arws.hrcalltracker

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log

/**
 * CallLogHelper — Reads the latest call log entry from Android's CallLog.Calls.
 *
 * After a call ends (IDLE detected by CallTrackingService),
 * this helper queries the call log database, sorts by date descending,
 * and extracts the most recent call's details.
 */
class CallLogHelper(private val context: Context) {

    companion object {
        private const val TAG = "CallLogHelper"
    }

    /**
     * Data class to hold extracted call details.
     */
    data class CallInfo(
        val phoneNumber: String,
        val callType: String,       // "Incoming", "Outgoing", "Missed"
        val duration: String,       // in seconds
        val date: Long,             // timestamp in milliseconds
        val formattedDate: String,  // human-readable date string
        val subscriptionId: Int     // SIM subscription ID
    )

    /**
     * Read the latest call log entry.
     *
     * @return CallInfo with the latest call details, or null if no entries found.
     */
    fun getLatestCallLog(): CallInfo? {
        var cursor: Cursor? = null

        try {
            // Query the call log, sorted by date descending (latest first)
            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE,
                    "subscription_id"  // SIM subscription ID column
                ),
                null,
                null,
                "${CallLog.Calls.DATE} DESC"  // Latest first
            )

            if (cursor != null && cursor.moveToFirst()) {
                // Extract fields from the first (latest) row
                val number = cursor.getString(
                    cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                ) ?: "Unknown"

                val typeCode = cursor.getInt(
                    cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                )
                val callType = mapCallType(typeCode)

                val duration = cursor.getString(
                    cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                ) ?: "0"

                val dateMillis = cursor.getLong(
                    cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
                )
                val formattedDate = formatDate(dateMillis)

                val subscriptionId = try {
                    cursor.getInt(cursor.getColumnIndexOrThrow("subscription_id"))
                } catch (e: Exception) {
                    -1 // Unknown SIM
                }

                val callInfo = CallInfo(
                    phoneNumber = number,
                    callType = callType,
                    duration = duration,
                    date = dateMillis,
                    formattedDate = formattedDate,
                    subscriptionId = subscriptionId
                )

                Log.d(TAG, "──────────────────────────────────")
                Log.d(TAG, "📋 Latest Call Log Entry:")
                Log.d(TAG, "   Number:  ${callInfo.phoneNumber}")
                Log.d(TAG, "   Type:    ${callInfo.callType}")
                Log.d(TAG, "   Duration: ${callInfo.duration} sec")
                Log.d(TAG, "   Date:    ${callInfo.formattedDate}")
                Log.d(TAG, "   SIM ID:  ${callInfo.subscriptionId}")
                Log.d(TAG, "──────────────────────────────────")

                return callInfo
            } else {
                Log.w(TAG, "No call log entries found")
                return null
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied: READ_CALL_LOG required", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error reading call log", e)
            return null
        } finally {
            cursor?.close()
        }
    }

    /**
     * Scan call log for entries newer than a given timestamp.
     * Used by PeriodicSyncWorker for batch uploads.
     */
    fun scanCallsSince(timestamp: Long): List<CallInfo> {
        val newCalls = mutableListOf<CallInfo>()
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE,
                    "subscription_id"
                ),
                "${CallLog.Calls.DATE} > ?",
                arrayOf(timestamp.toString()),
                "${CallLog.Calls.DATE} ASC" // old to new to process in order
            )

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)) ?: "Unknown"
                    val typeCode = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)) ?: "0"
                    val dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val subscriptionId = try {
                        cursor.getInt(cursor.getColumnIndexOrThrow("subscription_id"))
                    } catch (e: Exception) {
                        -1
                    }

                    newCalls.add(
                        CallInfo(
                            phoneNumber = number,
                            callType = mapCallType(typeCode),
                            duration = duration,
                            date = dateMillis,
                            formattedDate = formatDate(dateMillis),
                            subscriptionId = subscriptionId
                        )
                    )
                } while (cursor.moveToNext())
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for call log scan", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning call log", e)
        } finally {
            cursor?.close()
        }

        return newCalls
    }

    /**
     * Map Android call type integer to human-readable string.
     */
    private fun mapCallType(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "Incoming"
            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            CallLog.Calls.MISSED_TYPE -> "Missed"
            CallLog.Calls.REJECTED_TYPE -> "Rejected"
            else -> "Unknown"
        }
    }

    /**
     * Format a timestamp in milliseconds to a readable date string.
     * Example: "2026-03-19 10:40"
     */
    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}
