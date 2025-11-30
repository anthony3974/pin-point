package week11.st729217.pinpoint.pushNotification.service

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import week11.st729217.pinpoint.ONESIGNAL_APP_ID

class OneSignalSender {

    private val restApiKey = ""
    private val client = OkHttpClient()

    // NOTICE: We take 'targetUid' instead of 'targetDeviceId'
    fun sendNotificationToUser(targetUid: String, title: String, message: String) {
        val json = JSONObject()

        try {
            json.put("app_id", ONESIGNAL_APP_ID)

            json.put("target_channel", "push")

            // --- THE CHANGE IS HERE ---
            // Instead of "include_player_ids" (Device ID), we use "include_aliases" (User ID)
            val targetList = JSONArray().apply { put(targetUid) }
            val aliases = JSONObject().apply { put("external_id", targetList) }

            json.put("include_aliases", aliases)
            // --------------------------

            val contents = JSONObject().apply { put("en", message) }
            json.put("contents", contents)

            val headings = JSONObject().apply { put("en", title) }
            json.put("headings", headings)

        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("https://api.onesignal.com/notifications")
            .post(body)
            .addHeader("Authorization", "Basic $restApiKey")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OneSignal", "Failed to send notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("OneSignal", "Notification Sent to User $targetUid!")
                } else {
                    Log.e("OneSignal", "Error: ${response.body?.string()}")
                }
            }
        })
    }
}