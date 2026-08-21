// PrefsKeys.kt
package com.example.grepolisnotificationforwarder

/**
 * Single source of truth for ALL SharedPreferences keys and file name.
 */
object PrefsKeys {

    /** SharedPreferences file name */
    const val PREFS_FILE = "AppPrefs"

    // -------------------------------------------------------------------------
    // Player / Game settings
    // -------------------------------------------------------------------------
    const val PLAYER_NAME   = "PLAYER_NAME"
    const val PLAYER_WORLD  = "PLAYER_WORLD"

    // -------------------------------------------------------------------------
    // Discord Webhooks
    // -------------------------------------------------------------------------
    const val USER_WEBHOOK        = "USER_WEBHOOK"
    const val WEBHOOK_1_KEYWORDS  = "WEBHOOK_1_KEYWORDS"
    const val TAG_EVERYONE_1      = "TAG_EVERYONE_1"
    
    const val USER_WEBHOOK_2      = "USER_WEBHOOK_2"
    const val WEBHOOK_2_KEYWORDS  = "WEBHOOK_2_KEYWORDS"
    const val TAG_EVERYONE_2      = "TAG_EVERYONE_2"

    const val USER_WEBHOOK_3      = "USER_WEBHOOK_3"
    const val WEBHOOK_3_KEYWORDS  = "WEBHOOK_3_KEYWORDS"
    const val TAG_EVERYONE_3      = "TAG_EVERYONE_3"

    // -------------------------------------------------------------------------
    // App behaviour
    // -------------------------------------------------------------------------
    const val REMINDER_INTERVALS = "REMINDER_INTERVALS"
    const val PAUSE_UNTIL        = "PAUSE_UNTIL"
    const val ONLY_DURING_HOURS  = "ONLY_DURING_HOURS"
    const val START_HOUR         = "START_HOUR"
    const val END_HOUR           = "END_HOUR"
    const val PRIVACY_ACCEPTED   = "PRIVACY_ACCEPTED"
    const val USER_ID            = "USER_ID"

    // -------------------------------------------------------------------------
    // Last Attack Cache (for manual reply)
    // -------------------------------------------------------------------------
    const val LAST_ATTACK_TITLE = "LAST_ATTACK_TITLE"
    const val LAST_ATTACK_TEXT  = "LAST_ATTACK_TEXT"
    const val ATTACK_COUNT      = "ATTACK_COUNT"
    const val DAILY_ATTACK_COUNT = "DAILY_ATTACK_COUNT"
}
