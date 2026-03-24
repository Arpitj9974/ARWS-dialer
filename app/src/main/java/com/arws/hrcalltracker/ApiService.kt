package com.arws.hrcalltracker

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * ApiService — Sends call data to Google Apps Script with uniqueKey for idempotent uploads.
 *
 * Rules enforced:
 * - Rule 1: Idempotent upload — uniqueKey sent so server can reject duplicates
 * - Rule 2: uniqueKey included in every JSON payload
 * - Rule 8: Returns structured result; caller must check before marking synced
 * - Rule 15: Parses server response as "inserted" / "skipped_duplicate" / "error"
 */
class ApiService {

    companion object {
        private const val TAG = "ApiService"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * Result of an upload attempt.
     * - INSERTED: server accepted and appended the row
     * - SKIPPED_DUPLICATE: server found existing uniqueKey, row skipped
     * - ERROR: network/server error, should retry later
     */
    enum class UploadResult {
        INSERTED,
        SKIPPED_DUPLICATE,
        ERROR
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Sends call data to Google Apps Script.
     *
     * @param uniqueKey The unique identifier for this call record (Rule 2).
     *                  Format: normalizedNumber_dateMillis_duration_callType_simId
     * @return UploadResult indicating what the server did with this record.
     */
    fun sendCallDataSync(
        scriptUrl: String,
        hrName: String,
        phoneNumber: String,
        callType: String,
        duration: String,
        date: String,
        time: String,
        simName: String,
        uniqueKey: String
    ): UploadResult {
        if (scriptUrl.isEmpty()) return UploadResult.ERROR

        val json = JSONObject().apply {
            put("hr_name", hrName)
            put("phone_number", phoneNumber)
            put("call_type", callType)
            put("duration", duration)
            put("date", date)
            put("time", time)
            put("sim", simName)
            put("unique_key", uniqueKey)  // Rule 2: Send uniqueKey to Apps Script
        }

        val requestBody = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(scriptUrl)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                Log.d(TAG, "API Response: Code=${response.code}, Body=$bodyStr")

                if (!response.isSuccessful) {
                    Log.w(TAG, "   ❌ HTTP error: ${response.code}")
                    return@use UploadResult.ERROR
                }

                // Parse structured response from Apps Script (Rule 15)
                try {
                    val responseJson = JSONObject(bodyStr)
                    val status = responseJson.optString("status", "")
                    val action = responseJson.optString("action", "")

                    when {
                        action == "skipped_duplicate" -> {
                            Log.d(TAG, "   ⚠️ Server: SKIPPED_DUPLICATE for key=$uniqueKey")
                            UploadResult.SKIPPED_DUPLICATE
                        }
                        action == "invalid_data" -> {
                            Log.w(TAG, "   ❌ Server: INVALID_DATA for key=$uniqueKey")
                            UploadResult.ERROR
                        }
                        status == "success" -> {
                            Log.d(TAG, "   ✅ Server: INSERTED for key=$uniqueKey")
                            UploadResult.INSERTED
                        }
                        else -> {
                            // Legacy compatibility: treat "success" in body as inserted
                            if (bodyStr.contains("success")) UploadResult.INSERTED
                            else UploadResult.ERROR
                        }
                    }
                } catch (parseEx: Exception) {
                    // If response isn't valid JSON, fall back to string check
                    if (bodyStr.contains("success")) UploadResult.INSERTED
                    else UploadResult.ERROR
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during sendCallDataSync: ", e)
            UploadResult.ERROR
        }
    }
}
