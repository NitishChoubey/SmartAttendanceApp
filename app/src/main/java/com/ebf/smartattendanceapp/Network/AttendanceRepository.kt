package com.ebf.smartattendanceapp.Network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AttendanceRepository(private val client: OkHttpClient = OkHttpClient()) {
    private val baseUrl = "https://example.com/api" // Replace with real endpoint

    suspend fun markAttendance(qrValue: String): Boolean {
        val json = JSONObject().apply { put("qrValue", qrValue) }.toString()
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/attendance")
            .post(requestBody)
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}
