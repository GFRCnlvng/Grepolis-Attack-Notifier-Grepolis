package com.example.grepolisnotificationforwarder

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.work.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class DailyReportWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val client = OkHttpClient()
    private val REPORT_WEBHOOK = "https://discord.com/api/webhooks/1444640449066434591/tWASE25vLwOw7M597tdqF5VJ2REskAMxiOIaiJCWecWvLBb__KaNRag0LrOWHGsqiFyW"

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)
        val playerName = prefs.getString(PrefsKeys.PLAYER_NAME, "Unknown") ?: "Unknown"
        val dailyCount = prefs.getInt(PrefsKeys.DAILY_ATTACK_COUNT, 0)

        try {
            val message = "📊 **Daily Attack Report**\n" +
                    "**Player:** $playerName\n" +
                    "**Total Attacks Today:** $dailyCount"

            val json = JSONObject().apply {
                put("content", message)
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(REPORT_WEBHOOK)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // Reset the count for the next day
                    applicationContext.getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE).edit {
                        putInt(PrefsKeys.DAILY_ATTACK_COUNT, 0)
                    }
                    Log.d("DailyReportWorker", "Report sent successfully for $playerName")
                } else {
                    Log.e("DailyReportWorker", "Failed to send report: ${response.code}")
                    return Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("DailyReportWorker", "Report Failed", e)
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val workManager = WorkManager.getInstance(context)
            
            // Calculate initial delay for 23:59
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val initialDelay = calendar.timeInMillis - now
            
            val dailyRequest = PeriodicWorkRequestBuilder<DailyReportWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag("DAILY_REPORT")
                .build()
                
            workManager.enqueueUniquePeriodicWork(
                "DAILY_REPORT_WORK",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
            )
        }
    }
}
