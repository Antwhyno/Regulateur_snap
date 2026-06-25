plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.anti_scroll.blocker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.anti_scroll.blocker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}