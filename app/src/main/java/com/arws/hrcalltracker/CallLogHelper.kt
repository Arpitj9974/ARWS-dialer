package com.arws.hrcalltracker

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * CallLogHelper — Reads the latest call log entry from Android's CallLog.Calls.
 */
class CallLogHelper(private val context: Context) {

    companion object {
        private const val TAG = "CallLogHelper"
    }

    data class CallInfo(
        val phoneNumber: String,
        val callType: String,
        val duration: String,
        val dateMillis: Long,
        val date: String,    // dd/MM/yyyy
        val time: String,    // HH:mm:ss
        val subscriptionId: Int
    )

    fun getLatestCallLog(): CallInfo? {
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
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)) ?: "Unknown"
                val typeCode = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)) ?: "0"
                val dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
                
                val subscriptionId = try {
                    cursor.getInt(cursor.getColumnIndexOrThrow("subscription_id"))
                } catch (e: Exception) {
                    -1
                }

                return CallInfo(
                    phoneNumber = number,
                    callType = mapCallType(typeCode),
                    duration = duration,
                    dateMillis = dateMillis,
                    date = formatDateOnly(dateMillis),
                    time = formatTimeOnly(dateMillis),
                    subscriptionId = subscriptionId
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading latest call log", e)
        } finally {
            cursor?.close()
        }
        return null
    }

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
                "${CallLog.Calls.DATE} ASC"
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
                            dateMillis = dateMillis,
                            date = formatDateOnly(dateMillis),
                            time = formatTimeOnly(dateMillis),
                            subscriptionId = subscriptionId
                        )
                    )
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning calls", e)
        } finally {
            cursor?.close()
        }
        return newCalls
    }

    private fun mapCallType(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "Incoming"
            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            CallLog.Calls.MISSED_TYPE -> "Missed"
            CallLog.Calls.REJECTED_TYPE -> "Rejected"
            else -> "Unknown"
        }
    }

    private fun formatDateOnly(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun formatTimeOnly(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
