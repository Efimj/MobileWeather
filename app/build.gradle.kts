plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val javaVersion = JavaVersion.toVersion(libs.versions.jvmTarget.get())

android {
    namespace = "com.example.mobileweatherapp"
    compileSdk = libs.versions.androidCompileSdk.get().toIntOrNull()

    defaultConfig {

        applicationId = "com.weatherapp.freeweather.free.app.mobile.androidapp.weatherandroid"
        minSdk = libs.versions.androidMinSdk.get().toIntOrNull()
        targetSdk = libs.versions.androidTargetSdk.get().toIntOrNull()
        versionCode = libs.versions.versionCode.get().toIntOrNull()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Custom weather api
    implementation(projects.feature.openweatherapi)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)

    // Material theme
    implementation(libs.material)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Icons
    implementation(libs.androidx.material.icons.extended)

    // To handle GPS
    implementation(libs.play.services.location)
}