package com.arws.hrcalltracker

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ApiService — Sends call data to Google Sheets via Google Apps Script.
 *
 * Makes an asynchronous HTTP POST request with JSON payload.
 * The Google Apps Script endpoint appends the data as a new row in the sheet.
 */
class ApiService {

    companion object {
        private const val TAG = "ApiService"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Send call data to a specific HR's Google Sheet synchronously.
     *
     * @param scriptUrl   The unique Google Apps Script URL for this HR
     * @param hrName      The HR employee's name
     * @param employeeId  The HR employee's unique ID
     * @param phoneNumber The phone number from the call
     * @param callType    "Incoming", "Outgoing", or "Missed"
     * @param duration    Call duration in seconds
     * @param date        Formatted date string
     * @param simName     The SIM card used
     */
    fun sendCallDataSync(
        scriptUrl: String,
        hrName: String,
        employeeId: String,
        phoneNumber: String,
        callType: String,
        duration: String,
        date: String,
        simName: String
    ): Boolean {
        if (scriptUrl.isEmpty()) {
            Log.e(TAG, "❌ Cannot send data: Script URL is empty.")
            return false
        }

        // Build JSON payload as per Phase 2 requirements
        val json = JSONObject().apply {
            put("hr_name", hrName)
            put("employee_id", employeeId)
            put("phone_number", phoneNumber)
            put("call_type", callType)
            put("duration", duration)
            put("date", date)
            put("sim", simName)
        }

        val requestBody = json.toString().toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(scriptUrl)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: "Empty body"
                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Data sent successfully to $scriptUrl")
                    Log.d(TAG, "Server Response: $responseBody")
                    true
                } else {
                    Log.e(TAG, "❌ Server error: ${response.code} - ${response.message}")
                    Log.e(TAG, "❌ Error Body: $responseBody")
                    // Often GAS returns 200 OK with HTML error page if bad URL, we need to check the exact response to be sure.
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error sending data: ${e.message}")
            false
        }
    }
}
