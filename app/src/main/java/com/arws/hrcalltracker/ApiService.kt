package com.arws.hrcalltracker

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiService {

    companion object {
        private const val TAG = "ApiService"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false) // Handle redirects manually to preserve POST body
        .followSslRedirects(false)
        .build()

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

        val json = JSONObject().apply {
            put("hr_name", hrName)
            put("employee_id", employeeId)
            put("phone_number", phoneNumber)
            put("call_type", callType)
            put("duration", duration)
            put("date", date)
            put("sim", simName)
        }

        return postWithManualRedirect(scriptUrl, json.toString())
    }

    private fun postWithManualRedirect(url: String, jsonBody: String): Boolean {
        var currentUrl = url
        var attempts = 0
        val maxRedirects = 5

        while (attempts < maxRedirects) {
            val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(currentUrl)
                .post(requestBody)
                .build()

            var nextUrl: String? = null
            var finalSuccess = false

            try {
                client.newCall(request).execute().use { response ->
                    val code = response.code
                    val location = response.header("Location")
                    val body = response.body?.string() ?: ""

                    Log.d(TAG, "📡 URL: $currentUrl -> Code: $code")

                    if (code in 300..399 && location != null) {
                        Log.d(TAG, "↪️ Redirecting to: $location")
                        nextUrl = location
                    } else if (response.isSuccessful) {
                        Log.d(TAG, "✅ Success: $body")
                        finalSuccess = body.contains("success")
                        return finalSuccess
                    } else {
                        Log.e(TAG, "❌ Server Error ($code): $body")
                        return false
                    }
                }
                
                // If the .use block finished and we have a nextUrl, loop again
                if (nextUrl != null) {
                    currentUrl = nextUrl!!
                    attempts++
                } else {
                    return finalSuccess
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Network error: ${e.message}")
                return false
            }
        }

        Log.e(TAG, "❌ Too many redirects")
        return false
    }
}
