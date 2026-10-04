plugins {
    id("com.android.application")
}

android {
    namespace = "com.aprendegratisingles.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aprendegratisingles.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "6.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
