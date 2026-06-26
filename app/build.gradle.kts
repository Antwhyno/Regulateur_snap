plugins {
    id("com.android.application") version "8.3.2"
    id("org.jetbrains.kotlin.android") version "1.9.23"
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
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}