// GrepolisNotificationService.kt
package com.example.grepolisnotificationforwarder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class GrepolisNotificationService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val CHANNEL_ID = "GREPOLIS_ATTACK_CHANNEL"
    private val CLOUDFLARE_URL = "https://grepolisand.gfrce.workers.dev/"

    private val prefs get() = getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)

    private fun getPlayerName()      = prefs.getString(PrefsKeys.PLAYER_NAME, "Onbekend") ?: "Onbekend"
    private fun getPlayerWorld()     = prefs.getString(PrefsKeys.PLAYER_WORLD, "") ?: ""
    
    private fun getWebhook1()        = prefs.getString(PrefsKeys.USER_WEBHOOK, "") ?: ""
    private fun getWebhook2()        = prefs.getString(PrefsKeys.USER_WEBHOOK_2, "") ?: ""
    private fun getWebhook3()        = prefs.getString(PrefsKeys.USER_WEBHOOK_3, "") ?: ""
    private fun getWebhook4()        = prefs.getString(PrefsKeys.USER_WEBHOOK_4, "") ?: ""
    private fun getWebhook5()        = prefs.getString(PrefsKeys.USER_WEBHOOK_5, "") ?: ""
    private fun getWebhook1Keywords() = prefs.getString(PrefsKeys.WEBHOOK_1_KEYWORDS, "") ?: ""
    private fun getWebhook2Keywords() = prefs.getString(PrefsKeys.WEBHOOK_2_KEYWORDS, "") ?: ""
    private fun getWebhook3Keywords() = prefs.getString(PrefsKeys.WEBHOOK_3_KEYWORDS, "") ?: ""
    private fun getWebhook4Keywords() = prefs.getString(PrefsKeys.WEBHOOK_4_KEYWORDS, "") ?: ""
    private fun getWebhook5Keywords() = prefs.getString(PrefsKeys.WEBHOOK_5_KEYWORDS, "") ?: ""
    private fun shouldTagEveryone1() = prefs.getBoolean(PrefsKeys.TAG_EVERYONE_1, true)
    private fun shouldTagEveryone2() = prefs.getBoolean(PrefsKeys.TAG_EVERYONE_2, true)
    private fun shouldTagEveryone3() = prefs.getBoolean(PrefsKeys.TAG_EVERYONE_3, true)
    private fun shouldTagEveryone4() = prefs.getBoolean(PrefsKeys.TAG_EVERYONE_4, true)
    private fun shouldTagEveryone5() = prefs.getBoolean(PrefsKeys.TAG_EVERYONE_5, true)

    private fun getPauseUntil()      = prefs.getLong(PrefsKeys.PAUSE_UNTIL, 0L)

    private fun isWithinActiveHours(): Boolean {
        if (!prefs.getBoolean(PrefsKeys.ONLY_DURING_HOURS, false)) return true
        
        val start = prefs.getInt(PrefsKeys.START_HOUR, 23)
        val end   = prefs.getInt(PrefsKeys.END_HOUR, 7)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return if (start < end) {
            currentHour in start until end
        } else {
            // Overlays midnight (e.g. 23 to 07)
            currentHour >= start || currentHour < end
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val pkg = sbn.packageName
        if (pkg != "com.innogames.grepolis" && pkg != "com.innogames.enterprise.grepolis") return
        if (getPauseUntil() > System.currentTimeMillis()) return
        if (!isWithinActiveHours()) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (isAttackOrSupportNotification(title, text)) {
            val currentCount = prefs.getInt(PrefsKeys.ATTACK_COUNT, 0) + 1
            val dailyCount = prefs.getInt(PrefsKeys.DAILY_ATTACK_COUNT, 0) + 1
            
            prefs.edit().apply {
                putInt(PrefsKeys.ATTACK_COUNT, currentCount)
                putInt(PrefsKeys.DAILY_ATTACK_COUNT, dailyCount)
                putString(PrefsKeys.LAST_ATTACK_TITLE, title)
                putString(PrefsKeys.LAST_ATTACK_TEXT, text)
            }.apply()

            // Determine which webhook to use
            val targetWebhook = determineWebhook(title, text)

            sendAttackAlert(title, text, currentCount, targetWebhook)
            scheduleReminders(title, text, targetWebhook)
            showNotificationWithActions(title, text)
            launchPopup(title, text)
        }
    }

    private fun isAttackOrSupportNotification(title: String, text: String): Boolean {
        val combined = (title + " " + text).lowercase()
        return combined.contains("aanval") || combined.contains("attack") || combined.contains("aangevallen") ||
                combined.contains("angriff") || combined.contains("under attack") || combined.contains("aankomst") ||
                combined.contains("ondersteund") || combined.contains("support") || combined.contains("versterking")
    }

    /**
     * Logic to decide which Discord link to use based on keywords.
     */
    private fun determineWebhook(title: String, text: String): String {
        val combined = (title + " " + text).lowercase()
        val w1 = getWebhook1()
        val w2 = getWebhook2()
        val w3 = getWebhook3()
        val w4 = getWebhook4()
        val w5 = getWebhook5()
        val k1 = getWebhook1Keywords().lowercase().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val k2 = getWebhook2Keywords().lowercase().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val k3 = getWebhook3Keywords().lowercase().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val k4 = getWebhook4Keywords().lowercase().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val k5 = getWebhook5Keywords().lowercase().split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Check priorities in reverse (highest Link index first)
        for (key in k5) { if (combined.contains(key)) return if (w5.isNotEmpty()) w5 else w1 }
        for (key in k4) { if (combined.contains(key)) return if (w4.isNotEmpty()) w4 else w1 }
        for (key in k3) { if (combined.contains(key)) return if (w3.isNotEmpty()) w3 else w1 }
        for (key in k2) { if (combined.contains(key)) return if (w2.isNotEmpty()) w2 else w1 }
        for (key in k1) { if (combined.contains(key)) return w1 }

        return w1
    }

    private fun scheduleReminders(title: String, text: String, webhookUrl: String) {
        if (webhookUrl.isEmpty()) return
        
        val w1 = getWebhook1()
        val w2 = getWebhook2()
        val w3 = getWebhook3()
        val w4 = getWebhook4()
        val w5 = getWebhook5()

        val shouldTag = when (webhookUrl) {
            w5 -> if (w5.isNotEmpty()) shouldTagEveryone5() else shouldTagEveryone1()
            w4 -> if (w4.isNotEmpty()) shouldTagEveryone4() else shouldTagEveryone1()
            w3 -> if (w3.isNotEmpty()) shouldTagEveryone3() else shouldTagEveryone1()
            w2 -> if (w2.isNotEmpty()) shouldTagEveryone2() else shouldTagEveryone1()
            else -> shouldTagEveryone1()
        }
        
        val reminderIntervals = prefs.getStringSet(PrefsKeys.REMINDER_INTERVALS, setOf("1", "15", "30")) ?: setOf("1", "15", "30")

        WorkManager.getInstance(this).cancelAllWorkByTag("ATTACK_REMINDER")

        reminderIntervals.forEach { interval ->
            val minutes = interval.toIntOrNull() ?: return@forEach
            val data = workDataOf(
                "PLAYER_NAME"  to getPlayerName(),
                "ATTACK_TITLE" to title,
                "ATTACK_TEXT"  to text,
                "WEBHOOK_URL"  to webhookUrl,
                "MINUTES"      to minutes,
                "SHOULD_TAG"   to shouldTag
            )
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(minutes.toLong(), TimeUnit.MINUTES)
                .setInputData(data)
                .addTag("ATTACK_REMINDER")
                .build()
            WorkManager.getInstance(this).enqueue(request)
        }
    }

    private fun sendAttackAlert(title: String, text: String, count: Int, webhookUrl: String) {
        scope.launch {
            try {
                val playerName = getPlayerName()
                val playerWorld = getPlayerWorld()
                val display = if (playerWorld.isNotBlank()) "$playerName ($playerWorld)" else playerName
                
                val jsonAdmin = JSONObject().apply {
                    put("type", "attack_alert")
                    put("userName", display)
                    put("title", title)
                    put("text", text)
                    put("count", count)
                }
                client.newCall(Request.Builder().url(CLOUDFLARE_URL).post(jsonAdmin.toString().toRequestBody("application/json".toMediaType())).build()).execute().use { it.close() }

                if (webhookUrl.isNotEmpty()) {
                    val w1 = getWebhook1()
                    val w2 = getWebhook2()
                    val w3 = getWebhook3()
                    val w4 = getWebhook4()
                    val w5 = getWebhook5()

                    val tagStr = when (webhookUrl) {
                        w5 -> if (w5.isNotEmpty() && shouldTagEveryone5()) "@everyone " else ""
                        w4 -> if (w4.isNotEmpty() && shouldTagEveryone4()) "@everyone " else ""
                        w3 -> if (w3.isNotEmpty() && shouldTagEveryone3()) "@everyone " else ""
                        w2 -> if (w2.isNotEmpty() && shouldTagEveryone2()) "@everyone " else ""
                        else -> if (shouldTagEveryone1()) "@everyone " else ""
                    }

                    val discordContent = JSONObject().apply {
                        put("content", "${tagStr}🚨 **Grepolis Attack Alert**\n**Player:** $display\n**Title:** $title\n**Message:** $text\n**Total attacks:** $count")
                    }
                    client.newCall(Request.Builder().url(webhookUrl).post(discordContent.toString().toRequestBody("application/json".toMediaType())).build()).execute().use { it.close() }
                }
            } catch (e: Exception) {
                Log.e("GFW", "Error sending alert: ${e.message}")
            }
        }
    }

    private fun showNotificationWithActions(title: String, text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Aanvallen", NotificationManager.IMPORTANCE_HIGH))
        }
        val intent = Intent(this, ResponseActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("TEXT", text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 Grepolis Aanval!")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pi, true)
            .addAction(android.R.drawable.ic_menu_save, "Reageer", pi)
            .setAutoCancel(true)
            .build()
        nm.notify(1001, notification)
    }

    private fun launchPopup(title: String, text: String) {
        startActivity(Intent(this, ResponseActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("TEXT", text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
    }
}
