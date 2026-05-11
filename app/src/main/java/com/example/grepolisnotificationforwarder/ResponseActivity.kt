package com.example.grepolisnotificationforwarder

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ResponseActivity : ComponentActivity() {

    private val CLOUDFLARE_URL = "https://grepolisand.gfrce.workers.dev/"
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        window.setBackgroundDrawableResource(android.R.color.transparent)

        val attackTitle = intent.getStringExtra("TITLE") ?: "🚨 Attack Alert!"
        val attackText = intent.getStringExtra("TEXT") ?: ""

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ResponsePopup(attackTitle, attackText) { finish() }
                    }
                }
            }
        }
    }

    @Composable
    fun ResponsePopup(title: String, message: String, onDismiss: () -> Unit) {
        var isSending by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.width(320.dp).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Attack Response", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                if (message.isNotEmpty()) {
                    Text(message, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    Button(
                        onClick = { isSending = true; sendResponse("✅ Safe", title, message) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Safe") }

                    Button(
                        onClick = { isSending = true; sendResponse("🆘 Need Assistance", title, message) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("Need Assistance") }

                    OutlinedButton(
                        onClick = { isSending = true; pauseAndRespond(30, title, message) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) { Text("Pause Alerts (30m)") }
                }
            }
        }
    }

    private fun pauseAndRespond(minutes: Int, title: String, message: String) {
        val prefs = getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)
        val pauseUntil = System.currentTimeMillis() + (minutes * 60 * 1000L)
        prefs.edit().putLong(PrefsKeys.PAUSE_UNTIL, pauseUntil).apply()
        sendResponse("⏸ Paused for ${minutes}m", title, message)
    }

    private fun sendResponse(choice: String, originalAttack: String, originalText: String) {
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag("ATTACK_REMINDER")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                try {
                    val prefs = getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)
                    val playerName = prefs.getString(PrefsKeys.PLAYER_NAME, "Unknown") ?: "Unknown"
                    val userWebhook = prefs.getString(PrefsKeys.USER_WEBHOOK, "")?.trim() ?: ""
                    val userWebhook2 = prefs.getString(PrefsKeys.USER_WEBHOOK_2, "")?.trim() ?: ""
                    val w2Keywords = prefs.getString(PrefsKeys.WEBHOOK_2_KEYWORDS, "")?.lowercase()?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

                    val mediaType = "application/json; charset=utf-8".toMediaType()

                    // 1. Send to Cloudflare
                    val jsonAdmin = JSONObject().apply {
                        put("type", "user_response")
                        put("userName", playerName)
                        put("title", originalAttack)
                        put("text", choice)
                    }

                    val adminRequest = Request.Builder()
                        .url(CLOUDFLARE_URL)
                        .post(jsonAdmin.toString().toRequestBody(mediaType))
                        .build()
                    client.newCall(adminRequest).execute().close()

                    // 2. Determine which Discord Webhook to use and if we should tag
                    val overrideWebhook = intent.getStringExtra("WEBHOOK_OVERRIDE")
                    var targetWebhook = if (!overrideWebhook.isNullOrEmpty()) overrideWebhook else userWebhook
                    var shouldTag = if (targetWebhook == userWebhook2 && userWebhook2.isNotEmpty()) {
                        prefs.getBoolean(PrefsKeys.TAG_EVERYONE_2, true)
                    } else {
                        prefs.getBoolean(PrefsKeys.TAG_EVERYONE_1, true)
                    }
                    
                    if (overrideWebhook.isNullOrEmpty()) {
                        val combined = (originalAttack + " " + originalText).lowercase()
                        for (key in w2Keywords) {
                            if (combined.contains(key)) {
                                if (userWebhook2.isNotEmpty()) {
                                    targetWebhook = userWebhook2
                                    shouldTag = prefs.getBoolean(PrefsKeys.TAG_EVERYONE_2, true)
                                }
                                break
                            }
                        }
                    }

                    // 3. Send to Personal Discord
                    if (targetWebhook.isNotEmpty() && targetWebhook.startsWith("http")) {
                        val tagStr = if (shouldTag) "@everyone " else ""
                        val contentText = "${tagStr}📢 **User Response from $playerName**\n\n**Original Attack:** $originalAttack\n**Action Taken:** $choice"
                        val discordJson = JSONObject().apply {
                            put("content", contentText)
                        }

                        val discordRequest = Request.Builder()
                            .url(targetWebhook)
                            .post(discordJson.toString().toRequestBody(mediaType))
                            .build()
                        client.newCall(discordRequest).execute().close()
                    }
                } catch (e: Exception) {
                    Log.e("API", "Network Failure: ${e.message}")
                }
            }
            delay(200)
            finish()
        }
    }
}
