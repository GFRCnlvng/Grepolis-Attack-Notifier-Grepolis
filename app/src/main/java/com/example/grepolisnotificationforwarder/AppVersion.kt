// AppVersion.kt
package com.example.grepolisnotificationforwarder

/**
 * Central version definition — only edit this file when releasing a new version.
 *
 * Versioning guide:
 *   MAJOR → big redesign or breaking change        (e.g. 2.0.0.0)
 *   MINOR → new feature added                      (e.g. 1.1.0.0)
 *   PATCH → bug fix or small improvement           (e.g. 1.0.1.0)
 *   BUILD → internal build increment               (e.g. 1.0.0.2)
 *
 * To release a new version: bump the relevant number, reset lower ones to 0.
 * Example: fixing a bug in 1.1.0.0 → becomes 1.1.1.0
 */
object AppVersion {
    const val MAJOR = 1
    const val MINOR = 1
    const val PATCH = 2
    const val BUILD = 0

    /**
     * Full version string — no "v" prefix here.
     * Usage in UI: "v${AppVersion.NAME}" → "v1.0.0.1"
     */
    const val NAME = "$MAJOR.$MINOR.$PATCH.$BUILD"

    /** Highlights for this version */
    val CHANGELOG = """
        • Daily Attack Report: Nightly summary sent to Discord at 23:59.
        • Manual Reply: Backup button to respond if notifications were missed.
        • Build Stability: Resolved 50+ errors by removing legacy duplicates.
        • Version Sync: Aligned all metadata to v1.1.2.0 (Code 4).
    """.trimIndent()

    /** Features currently in beta — shown as BETA badge in UI */
    // val BETA_FEATURES = emptySet<String>()
}