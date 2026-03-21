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
        .followRedirects(false)
        .followSslRedirects(false)
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
            try {
                client.newCall(request).execute().use { response ->
                    val code = response.code
                    val location = response.header("Location")
                    val body = response.body?.string() ?: ""

                    if (code in 300..399 && location != null) {
                        nextUrl = location
                    } else if (response.isSuccessful) {
                        return body.contains("success")
                    } else {
                        return false
                    }
                }
                
                if (nextUrl != null) {
                    currentUrl = nextUrl!!
                    attempts++
                } else {
                    return false
                }
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }
}
