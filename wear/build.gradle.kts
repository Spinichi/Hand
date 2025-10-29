plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.example.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.wear"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    useLibrary("wear-sdk")
}

dependencies {
    implementation(project(":core"))

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.wear.compose:compose-material:1.5.4") // Wear Compose
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Wear 관련
    implementation(libs.play.services.wearable)

    // 기타
    implementation(libs.androidx.core.splashscreen)

    // 테스트
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.wear.compose:compose-foundation:1.5.4")
    implementation("androidx.wear.compose:compose-navigation:1.5.4")
}
