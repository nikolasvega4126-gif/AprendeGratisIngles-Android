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
        versionCode = 49
        versionName = "8.4.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
