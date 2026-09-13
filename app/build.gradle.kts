import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    val appVersionFile = file("src/main/java/com/example/grepolisnotificationforwarder/AppVersion.kt")
    var major = "1"
    var minor = "1"
    var patch = "2"
    var build = "1"

    if (appVersionFile.exists()) {
        appVersionFile.forEachLine { line ->
            if (line.contains("const val MAJOR =")) major = line.substringAfter("=").trim()
            if (line.contains("const val MINOR =")) minor = line.substringAfter("=").trim()
            if (line.contains("const val PATCH =")) patch = line.substringAfter("=").trim()
            if (line.contains("const val BUILD =")) build = line.substringAfter("=").trim()
        }
    }

    val versionPropsFile = file("version.properties")
    val versionProps = Properties()
    var currentVersionCode = 4
    var currentBuildNumber = build.toIntOrNull() ?: 1

    if (versionPropsFile.exists()) {
        versionPropsFile.inputStream().use { versionProps.load(it) }
        currentVersionCode = versionProps.getProperty("versionCode")?.toIntOrNull() ?: 4
        currentBuildNumber = versionProps.getProperty("buildNumber")?.toIntOrNull() ?: currentBuildNumber
    } else {
        versionProps.setProperty("versionCode", currentVersionCode.toString())
        versionProps.setProperty("buildNumber", currentBuildNumber.toString())
        versionPropsFile.outputStream().use { versionProps.store(it, "Initial Automated Build Properties") }
    }

    val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

    if (isReleaseBuild) {
        currentVersionCode += 1
        currentBuildNumber += 1
        
        versionProps.setProperty("versionCode", currentVersionCode.toString())
        versionProps.setProperty("buildNumber", currentBuildNumber.toString())
        versionPropsFile.outputStream().use { versionProps.store(it, "Automated Build Properties") }
        
        if (appVersionFile.exists()) {
            val content = appVersionFile.readText()
            val updatedContent = content.replace(
                Regex("""const\s+val\s+BUILD\s*=\s*\d+"""),
                "const val BUILD = $currentBuildNumber"
            )
            appVersionFile.writeText(updatedContent)
        }
        build = currentBuildNumber.toString()
    }

    namespace = "com.example.grepolisnotificationforwarder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.grepolisnotificationforwarder"
        minSdk = 24
        targetSdk = 34
        versionCode = currentVersionCode
        versionName = "$major.$minor.$patch.$build"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)


    // Added dependencies
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // WorkManager for reminders
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
