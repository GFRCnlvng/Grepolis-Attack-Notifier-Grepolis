// MainActivity.kt
package com.example.grepolisnotificationforwarder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Schedule the daily report at 23:59
        DailyReportWorker.schedule(this)

        setContent {
            MaterialTheme {
                MainWizard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWizard() {
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)
    val prefs = context.getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)

    var currentPage by remember { mutableIntStateOf(1) }

    // Settings state
    var playerName      by remember { mutableStateOf(prefs.getString(PrefsKeys.PLAYER_NAME, "") ?: "") }
    var playerWorld     by remember { mutableStateOf(prefs.getString(PrefsKeys.PLAYER_WORLD, "") ?: "") }
    var webhookUrl      by remember { mutableStateOf(prefs.getString(PrefsKeys.USER_WEBHOOK, "") ?: "") }
    var webhook1Keywords by remember { mutableStateOf(prefs.getString(PrefsKeys.WEBHOOK_1_KEYWORDS, "") ?: "") }
    var webhookUrl2     by remember { mutableStateOf(prefs.getString(PrefsKeys.USER_WEBHOOK_2, "") ?: "") }
    var webhook2Keywords by remember { mutableStateOf(prefs.getString(PrefsKeys.WEBHOOK_2_KEYWORDS, "") ?: "") }
    var webhookUrl3     by remember { mutableStateOf(prefs.getString(PrefsKeys.USER_WEBHOOK_3, "") ?: "") }
    var webhook3Keywords by remember { mutableStateOf(prefs.getString(PrefsKeys.WEBHOOK_3_KEYWORDS, "") ?: "") }
    var webhookUrl4     by remember { mutableStateOf(prefs.getString(PrefsKeys.USER_WEBHOOK_4, "") ?: "") }
    var webhook4Keywords by remember { mutableStateOf(prefs.getString(PrefsKeys.WEBHOOK_4_KEYWORDS, "") ?: "") }
    var webhookUrl5     by remember { mutableStateOf(prefs.getString(PrefsKeys.USER_WEBHOOK_5, "") ?: "") }
    var webhook5Keywords by remember { mutableStateOf(prefs.getString(PrefsKeys.WEBHOOK_5_KEYWORDS, "") ?: "") }
    
    var tagEveryone1    by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.TAG_EVERYONE_1, true)) }
    var tagEveryone2    by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.TAG_EVERYONE_2, true)) }
    var tagEveryone3    by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.TAG_EVERYONE_3, true)) }
    var tagEveryone4    by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.TAG_EVERYONE_4, true)) }
    var tagEveryone5    by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.TAG_EVERYONE_5, true)) }

    var onlyDuringHours by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.ONLY_DURING_HOURS, false)) }
    var startHour       by remember { mutableIntStateOf(prefs.getInt(PrefsKeys.START_HOUR, 23)) }
    var endHour         by remember { mutableIntStateOf(prefs.getInt(PrefsKeys.END_HOUR, 7)) }

    val defaultReminders = setOf("1", "15", "30")
    val savedReminders = prefs.getStringSet(PrefsKeys.REMINDER_INTERVALS, defaultReminders) ?: defaultReminders
    val selectedReminders = remember { mutableStateListOf<String>().apply { addAll(savedReminders) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("A.N.D. Grepolis v${AppVersion.NAME}") },
                actions = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 1) {
                        Button(onClick = { currentPage-- }) { Text("← Back") }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Text(
                        text = "$currentPage / 5",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (currentPage < 5) {
                        Button(onClick = { currentPage++ }) { Text("Next →") }
                    } else {
                        Button(onClick = {
                            prefs.edit {
                                putString(PrefsKeys.PLAYER_NAME,      playerName)
                                putString(PrefsKeys.PLAYER_WORLD,     playerWorld)
                                putString(PrefsKeys.USER_WEBHOOK,     webhookUrl)
                                putString(PrefsKeys.WEBHOOK_1_KEYWORDS, webhook1Keywords)
                                putString(PrefsKeys.USER_WEBHOOK_2,   webhookUrl2)
                                putString(PrefsKeys.WEBHOOK_2_KEYWORDS, webhook2Keywords)
                                putString(PrefsKeys.USER_WEBHOOK_3,   webhookUrl3)
                                putString(PrefsKeys.WEBHOOK_3_KEYWORDS, webhook3Keywords)
                                putString(PrefsKeys.USER_WEBHOOK_4,   webhookUrl4)
                                putString(PrefsKeys.WEBHOOK_4_KEYWORDS, webhook4Keywords)
                                putString(PrefsKeys.USER_WEBHOOK_5,   webhookUrl5)
                                putString(PrefsKeys.WEBHOOK_5_KEYWORDS, webhook5Keywords)
                                putBoolean(PrefsKeys.TAG_EVERYONE_1,   tagEveryone1)
                                putBoolean(PrefsKeys.TAG_EVERYONE_2,   tagEveryone2)
                                putBoolean(PrefsKeys.TAG_EVERYONE_3,   tagEveryone3)
                                putBoolean(PrefsKeys.TAG_EVERYONE_4,   tagEveryone4)
                                putBoolean(PrefsKeys.TAG_EVERYONE_5,   tagEveryone5)
                                putBoolean(PrefsKeys.ONLY_DURING_HOURS, onlyDuringHours)
                                putInt(PrefsKeys.START_HOUR, startHour)
                                putInt(PrefsKeys.END_HOUR, endHour)
                                putStringSet(PrefsKeys.REMINDER_INTERVALS, selectedReminders.toSet())
                            }
                            Toast.makeText(context, "✅ Settings Saved!", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        }) { Text("Finish & Save") }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (currentPage) {
                1 -> Page1_Logo()
                2 -> Page2_Welcome(playerName, playerWorld, webhookUrl, webhookUrl2, webhookUrl3, webhookUrl4, webhookUrl5)
                3 -> Page3_Setup(
                    playerName, { playerName = it },
                    playerWorld, { playerWorld = it },
                    webhookUrl, { webhookUrl = it },
                    webhook1Keywords, { webhook1Keywords = it },
                    tagEveryone1, { tagEveryone1 = it },
                    webhookUrl2, { webhookUrl2 = it },
                    webhook2Keywords, { webhook2Keywords = it },
                    tagEveryone2, { tagEveryone2 = it },
                    webhookUrl3, { webhookUrl3 = it },
                    webhook3Keywords, { webhook3Keywords = it },
                    tagEveryone3, { tagEveryone3 = it },
                    webhookUrl4, { webhookUrl4 = it },
                    webhook4Keywords, { webhook4Keywords = it },
                    tagEveryone4, { tagEveryone4 = it },
                    webhookUrl5, { webhookUrl5 = it },
                    webhook5Keywords, { webhook5Keywords = it },
                    tagEveryone5, { tagEveryone5 = it }
                )
                4 -> Page5_Reminders(
                    selectedReminders, 
                    onlyDuringHours, { onlyDuringHours = it },
                    startHour, { startHour = it },
                    endHour, { endHour = it },
                    context
                )
                5 -> Page6_Permissions(context)
            }
        }
    }
}

@Composable
fun Page1_Logo() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(350.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun Page2_Welcome(playerName: String, playerWorld: String, w1: String, w2: String, w3: String, w4: String, w5: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)
    
    val webhooks = listOf(
        Triple(w1, "Link 1", MaterialTheme.colorScheme.primary),
        Triple(w2, "Link 2", MaterialTheme.colorScheme.tertiary),
        Triple(w3, "Link 3", Color(0xFF6200EE)),
        Triple(w4, "Link 4", Color(0xFF00796B)),
        Triple(w5, "Link 5", Color(0xFFC2185B))
    ).filter { it.first.isNotEmpty() }

    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Welcome!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "This app forwards Grepolis attack alerts to Discord automatically.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("What's New in v${AppVersion.NAME}:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(AppVersion.CHANGELOG, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Privacy",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Important: This app ONLY forwards Grepolis attack notifications. No other messages, personal data, or phone information is ever collected or shared.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        val lastTitle = prefs.getString(PrefsKeys.LAST_ATTACK_TITLE, "") ?: ""
        val lastText = prefs.getString(PrefsKeys.LAST_ATTACK_TEXT, "") ?: ""

        if (lastTitle.isNotEmpty()) {
            Text("Recent Attack: $lastTitle", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Chunk webhooks into pairs for row layout
                webhooks.chunked(2).forEach { rowWebhooks ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowWebhooks.forEach { (url, label, color) ->
                            Button(
                                onClick = {
                                    val intent = Intent(context, ResponseActivity::class.java).apply {
                                        putExtra("TITLE", lastTitle)
                                        putExtra("TEXT", lastText)
                                        putExtra("WEBHOOK_OVERRIDE", url)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = color)
                            ) {
                                Text("Reply $label", textAlign = TextAlign.Center, fontSize = 12.sp)
                            }
                        }
                        if (rowWebhooks.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // Manual Reply Fallback if no attack is cached
            Button(
                onClick = {
                    val intent = Intent(context, ResponseActivity::class.java).apply {
                        putExtra("TITLE", "Manual Report")
                        putExtra("TEXT", "Manual response initiated by player.")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Send Manual Reply (Backup)")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Test Connectivity:", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        webhooks.chunked(2).forEach { rowWebhooks ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowWebhooks.forEach { (url, label, color) ->
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val client = OkHttpClient()
                                    val json = JSONObject().apply {
                                        val displayPlayer = if (playerName.isBlank()) "Unknown Player" else playerName
                                        val displayWorld = if (playerWorld.isBlank()) "" else " ($playerWorld)"
                                        val kwKey = when(label) {
                                            "Link 1" -> PrefsKeys.WEBHOOK_1_KEYWORDS
                                            "Link 2" -> PrefsKeys.WEBHOOK_2_KEYWORDS
                                            "Link 3" -> PrefsKeys.WEBHOOK_3_KEYWORDS
                                            "Link 4" -> PrefsKeys.WEBHOOK_4_KEYWORDS
                                            "Link 5" -> PrefsKeys.WEBHOOK_5_KEYWORDS
                                            else -> ""
                                        }
                                        val kw = if (kwKey.isNotEmpty()) prefs.getString(kwKey, "") ?: "" else ""
                                        val kwMsg = if (kw.isNotEmpty()) "\nKeywords: $kw" else ""
                                        put("content", "🔔 **Grepolis Forwarder Test Message**\nConnection test for **$label** from $displayPlayer$displayWorld! ✅$kwMsg")
                                    }
                                    
                                    // Also send to admin if Link 1
                                    if (label == "Link 1") {
                                        val adminWebhook = "https://discord.com/api/webhooks/1444232699819724973/hKnuqwcCe75NtUNEgG_wd3D7yy9sTaVpiB7WbjyRsKpHNEDy22nMJ4JgsJvGmPneJzxA"
                                        val adminRequest = Request.Builder()
                                            .url(adminWebhook)
                                            .post(json.toString().toRequestBody("application/json".toMediaType()))
                                            .build()
                                        client.newCall(adminRequest).execute().close()
                                    }

                                    val request = Request.Builder()
                                        .url(url)
                                        .post(json.toString().toRequestBody("application/json".toMediaType()))
                                        .build()
                                    client.newCall(request).execute().use { res ->
                                        launch(Dispatchers.Main) {
                                            if (res.isSuccessful) Toast.makeText(context, "Test sent to $label! ✅", Toast.LENGTH_SHORT).show()
                                            else Toast.makeText(context, "$label Failed: ${res.code}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    launch(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Text("Test $label", textAlign = TextAlign.Center, fontSize = 12.sp)
                    }
                }
                if (rowWebhooks.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (webhooks.isEmpty()) {
            Text("Set up a Discord Webhook in step 3 to test connectivity.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun Page3_Setup(
    playerName: String, onPlayerNameChange: (String) -> Unit,
    playerWorld: String, onPlayerWorldChange: (String) -> Unit,
    webhookUrl: String, onWebhookUrlChange: (String) -> Unit,
    webhook1Keywords: String, onWebhook1KeywordsChange: (String) -> Unit,
    tagEveryone1: Boolean, onTagEveryone1Change: (Boolean) -> Unit,
    webhookUrl2: String, onWebhookUrl2Change: (String) -> Unit,
    webhook2Keywords: String, onWebhook2KeywordsChange: (String) -> Unit,
    tagEveryone2: Boolean, onTagEveryone2Change: (Boolean) -> Unit,
    webhookUrl3: String, onWebhookUrl3Change: (String) -> Unit,
    webhook3Keywords: String, onWebhook3KeywordsChange: (String) -> Unit,
    tagEveryone3: Boolean, onTagEveryone3Change: (Boolean) -> Unit,
    webhookUrl4: String, onWebhookUrl4Change: (String) -> Unit,
    webhook4Keywords: String, onWebhook4KeywordsChange: (String) -> Unit,
    tagEveryone4: Boolean, onTagEveryone4Change: (Boolean) -> Unit,
    webhookUrl5: String, onWebhookUrl5Change: (String) -> Unit,
    webhook5Keywords: String, onWebhook5KeywordsChange: (String) -> Unit,
    tagEveryone5: Boolean, onTagEveryone5Change: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Count how many links have URLs to show them initially
    val initialVisibleLinks = listOf(webhookUrl2, webhookUrl3, webhookUrl4, webhookUrl5)
        .count { it.isNotBlank() } + 1
    var visibleLinks by remember { mutableIntStateOf(maxOf(1, initialVisibleLinks)) }

    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("Account Setup", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = onPlayerNameChange,
            label = { Text("Player Name") },
            placeholder = { Text("e.g. ingame name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = playerWorld,
            onValueChange = onPlayerWorldChange,
            label = { Text("World ID (Optional)") },
            placeholder = { Text("e.g. en123") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (playerName.isNotBlank()) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val client = OkHttpClient()
                            val json = JSONObject().apply {
                                val display = if (playerWorld.isNotBlank()) "$playerName ($playerWorld)" else playerName
                                put("content", "📝 **Player Registered**\n**Name:** $display")
                            }
                            val request = Request.Builder()
                                .url("https://discord.com/api/webhooks/1444232699819724973/hKnuqwcCe75NtUNEgG_wd3D7yy9sTaVpiB7WbjyRsKpHNEDy22nMJ4JgsJvGmPneJzxA")
                                .post(json.toString().toRequestBody("application/json".toMediaType()))
                                .build()
                            client.newCall(request).execute().use { res ->
                                launch(Dispatchers.Main) {
                                    if (res.isSuccessful) Toast.makeText(context, "Name confirmed! ✅", Toast.LENGTH_SHORT).show()
                                    else Toast.makeText(context, "Failed: ${res.code}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            launch(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter a name first!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Player Name")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Discord Routing", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Dynamic Webhook list
        val webhookList = listOf(
            WebhookConfig("Link 1", webhookUrl, onWebhookUrlChange, webhook1Keywords, onWebhook1KeywordsChange, tagEveryone1, onTagEveryone1Change, MaterialTheme.colorScheme.secondary),
            WebhookConfig("Link 2", webhookUrl2, onWebhookUrl2Change, webhook2Keywords, onWebhook2KeywordsChange, tagEveryone2, onTagEveryone2Change, MaterialTheme.colorScheme.tertiary),
            WebhookConfig("Link 3", webhookUrl3, onWebhookUrl3Change, webhook3Keywords, onWebhook3KeywordsChange, tagEveryone3, onTagEveryone3Change, Color(0xFF6200EE)),
            WebhookConfig("Link 4", webhookUrl4, onWebhookUrl4Change, webhook4Keywords, onWebhook4KeywordsChange, tagEveryone4, onTagEveryone4Change, Color(0xFF00796B)),
            WebhookConfig("Link 5", webhookUrl5, onWebhookUrl5Change, webhook5Keywords, onWebhook5KeywordsChange, tagEveryone5, onTagEveryone5Change, Color(0xFFC2185B))
        )

        for (i in 0 until visibleLinks) {
            val config = webhookList[i]
            WebhookItem(config, playerName, playerWorld, scope, context)
            if (i < visibleLinks - 1) Spacer(modifier = Modifier.height(16.dp))
        }

        if (visibleLinks < 5) {
            OutlinedButton(
                onClick = { visibleLinks++ },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Add another link (+)")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("If message contains keywords, the respective Link is used. Link 1 is the default.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

data class WebhookConfig(
    val label: String,
    val url: String,
    val onUrlChange: (String) -> Unit,
    val keywords: String,
    val onKeywordsChange: (String) -> Unit,
    val tagEveryone: Boolean,
    val onTagEveryoneChange: (Boolean) -> Unit,
    val color: Color
)

@Composable
fun WebhookItem(config: WebhookConfig, playerName: String, playerWorld: String, scope: CoroutineScope, context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, config.color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(config.label, style = MaterialTheme.typography.labelLarge, color = config.color)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = config.url,
                onValueChange = config.onUrlChange,
                label = { Text("Webhook URL") },
                placeholder = { Text("https://discord.com/api/webhooks/...") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = config.tagEveryone, onCheckedChange = config.onTagEveryoneChange)
                Text("Tag @everyone", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedTextField(
                value = config.keywords,
                onValueChange = config.onKeywordsChange,
                label = { Text("Keywords (Optional)") },
                placeholder = { Text("e.g. Asine, Chios") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (config.url.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val client = OkHttpClient()
                                val kw = if (config.keywords.isNotBlank()) "\n**Keywords:** ${config.keywords}" else ""
                                val display = if (playerWorld.isNotBlank()) "$playerName ($playerWorld)" else playerName
                                val json = JSONObject().apply {
                                    put("content", "🔔 **Grepolis Forwarder Test: ${config.label}**\nVerified connection for **$display**!$kw ✅")
                                }
                                val request = Request.Builder().url(config.url).post(json.toString().toRequestBody("application/json".toMediaType())).build()
                                client.newCall(request).execute().use { res ->
                                    launch(Dispatchers.Main) {
                                        if (res.isSuccessful) Toast.makeText(context, "${config.label} Test Sent! ✅", Toast.LENGTH_SHORT).show()
                                        else Toast.makeText(context, "Failed: ${res.code}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                launch(Dispatchers.Main) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Enter ${config.label} Webhook first!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = config.color)
            ) {
                Text("Test ${config.label}")
            }
        }
    }
}

@Composable
fun Page5_Reminders(
    selectedReminders: SnapshotStateList<String>,
    onlyDuringHours: Boolean, onOnlyDuringHoursChange: (Boolean) -> Unit,
    startHour: Int, onStartHourChange: (Int) -> Unit,
    endHour: Int, onEndHourChange: (Int) -> Unit,
    context: Context
) {
    val options = listOf("1", "5", "10", "15", "30")
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Reminders", style = MaterialTheme.typography.headlineMedium)
        options.forEach { minutes ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = selectedReminders.contains(minutes), onCheckedChange = { if (it) selectedReminders.add(minutes) else selectedReminders.remove(minutes) })
                Text("After $minutes min")
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("Time Restrictions", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = onlyDuringHours, onCheckedChange = onOnlyDuringHoursChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Only forward during specific hours")
        }
        
        if (onlyDuringHours) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From (Hour)", style = MaterialTheme.typography.bodySmall)
                    HourDropdown(startHour, onStartHourChange)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("To (Hour)", style = MaterialTheme.typography.bodySmall)
                    HourDropdown(endHour, onEndHourChange)
                }
            }
            Text(
                "Example: 23:00 to 07:00 covers the night bonus.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        PauseControls(context)
    }
}

@Composable
fun HourDropdown(selectedHour: Int, onHourChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(String.format(Locale.US, "%02d:00", selectedHour))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (0..23).forEach { hour ->
                DropdownMenuItem(
                    text = { Text(String.format(Locale.US, "%02d:00", hour)) },
                    onClick = {
                        onHourChange(hour)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun Page6_Permissions(context: Context) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Permissions", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("1. Notification Access") }
        Button(onClick = { val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = Uri.fromParts("package", context.packageName, null) }; context.startActivity(intent) }, modifier = Modifier.fillMaxWidth()) { Text("2. Battery Unrestricted") }
        Spacer(modifier = Modifier.height(32.dp))
        // Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/aTD5knVy"))) }) { Text("Join Discord Support") }
        Text("Support link deactivated.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PauseControls(context: Context) {
    val prefs = context.getSharedPreferences(PrefsKeys.PREFS_FILE, Context.MODE_PRIVATE)
    var expanded by remember { mutableStateOf(false) }
    var pauseUntil by remember { mutableLongStateOf(prefs.getLong(PrefsKeys.PAUSE_UNTIL, 0L)) }
    val statusText = if (pauseUntil > System.currentTimeMillis()) "Paused (${TimeUnit.MILLISECONDS.toMinutes(pauseUntil - System.currentTimeMillis())}m)" else "App Active"
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(statusText) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Resume" to 0, "15m" to 15, "30m" to 30, "1h" to 60).forEach { (label, mins) ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    val time = if (mins == 0) 0L else System.currentTimeMillis() + mins * 60000L
                    prefs.edit { putLong(PrefsKeys.PAUSE_UNTIL, time) }
                    pauseUntil = time
                    expanded = false
                })
            }
        }
    }
}
