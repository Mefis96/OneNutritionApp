
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// Leer la clave de Google Maps desde local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use {
            load(it)
        }
    }
}

android {
    namespace = "com.example.onenutrition"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.onenutrition"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // Clave API de Google Maps
        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Dependencias principales de Android
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Dependencias para pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Autenticación biométrica
    implementation("androidx.biometric:biometric:1.1.0")

    // Firebase - Plataforma de versiones
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Glide - Carga de imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Google Play Services - Geolocalización
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Google Maps SDK para Android
    implementation("com.google.android.gms:play-services-maps:19.2.0")

    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
}
