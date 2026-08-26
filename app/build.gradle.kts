// app/build.gradle.kts
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.nogboardinset"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nogboardinset"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        checkReleaseBuilds = false
    }

    buildFeatures {
        compose = false
        viewBinding = false
        dataBinding = false
    }
}

dependencies {
    // JAR locale: scaricalo da https://mvnrepository.com/artifact/de.robv.android.xposed/api/82
    // e mettilo in app/libs/XposedBridgeApi-82.jar
    compileOnly(files("libs/XposedBridgeApi-82.jar"))
}
