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
        // By default, OkHttp handles redirects (followRedirects = true). 
        // Google Apps Script requires this native handling so a 302 POST redirect converts to a GET.
        .build()

    fun sendCallDataSync(
        scriptUrl: String,
        hrName: String,
        phoneNumber: String,
        callType: String,
        duration: String,
        date: String, // dd/MM/yyyy
        time: String, // HH:mm:ss
        simName: String
    ): Boolean {
        if (scriptUrl.isEmpty()) return false

        val json = JSONObject().apply {
            put("hr_name", hrName)
            put("phone_number", phoneNumber)
            put("call_type", callType)
            put("duration", duration)
            put("date", date)
            put("time", time)
            put("sim", simName)
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
                response.isSuccessful && bodyStr.contains("success")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during sendCallDataSync: ", e)
            false
        }
    }
}
