plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution") version "5.2.0"
    id("kotlin-parcelize")
}

android {
    namespace = "com.hand.hand"
    compileSdk = 36
    buildFeatures {
        compose = true  //ㅉ Compose 사용
        buildConfig = true  // BuildConfig 활성화
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"  // 최신 Compose Compiler
    }
    defaultConfig {
        applicationId = "com.hand.hand"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // GMS API 설정 (환경변수 또는 local.properties에서 읽기)
        buildConfigField("String", "GMS_BASE_URL", "\"${System.getenv("GMS_BASE_URL") ?: "https://gms.ssafy.io/gmsapi/api.openai.com/"}\"")
        buildConfigField("String", "GMS_API_KEY", "\"${System.getenv("GMS_API_KEY") ?: "S13P32A106-1bd9c436-0876-44c9-bdf2-bb77386d352c"}\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("androidx.compose:compose-bom:2024.10.00")) // BOM
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Accompanist Pager for onboarding carousel
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    // Firebase Cloud Messaging (BOM 34+ 부터 KTX 기능이 기본 포함)
    implementation("com.google.firebase:firebase-messaging")

    // Wearable communication
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // 레트로핏 & gson 컨버터 & 로깅 인터셉터
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

}

firebaseAppDistribution {
    serviceCredentialsFile = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON") // Jenkins가 주입
    appId = System.getenv("FIREBASE_APP_ID")
    groups = "Hand_A106"
    releaseNotes = System.getenv("RELEASE_NOTES")
}
