plugins {
    id("com.android.application")
}

android {
    namespace = "com.aprendegratisingles.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aprendegratisingles.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
