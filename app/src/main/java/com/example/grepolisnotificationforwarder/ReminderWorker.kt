package com.example.grepolisnotificationforwarder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val client = OkHttpClient()
    private val CHANNEL_ID = "GREPOLIS_REMINDER_CHANNEL"

    override suspend fun doWork(): Result {
        val playerName = inputData.getString("PLAYER_NAME") ?: "Unknown"
        val attackTitle = inputData.getString("ATTACK_TITLE") ?: "Unknown Title"
        val attackText = inputData.getString("ATTACK_TEXT") ?: "Unknown Message"
        val webhookUrl = inputData.getString("WEBHOOK_URL") ?: return Result.failure()
        val minutes = inputData.getInt("MINUTES", 0)
        val shouldTag = inputData.getBoolean("SHOULD_TAG", true)

        // 1. Send to Discord
        try {
            val tagStr = if (shouldTag) "@everyone " else ""
            val message = "⚠️ **${tagStr}NO RESPONSE RECEIVED**\n" +
                    "**Player:** $playerName\n" +
                    "**Time passed:** $minutes minutes\n" +
                    "**Attack:** $attackTitle: $attackText"

            val json = JSONObject().apply {
                put("content", message)
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()

            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Discord Reminder Failed", e)
        }

        // 2. Local Reminder (Push notification that opens the Response Activity)
        showLocalReminder(attackTitle, attackText, minutes)

        return Result.success()
    }

    private fun showLocalReminder(title: String, text: String, mins: Int) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_HIGH))
        }

        val intent = Intent(applicationContext, ResponseActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("TEXT", text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        val pi = PendingIntent.getActivity(
            applicationContext, 
            System.currentTimeMillis().toInt(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 ATTENTION: NO RESPONSE ($mins min)")
            .setContentText("Tap to respond to: $title")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true) // This will pop up the activity on most devices
            .setAutoCancel(true)
            .build()

        nm.notify(2002, notification)
    }
}