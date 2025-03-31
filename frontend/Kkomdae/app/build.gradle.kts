import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
   // id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if(localPropertiesFile.exists()){
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.pizza.kkomdae"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pizza.kkomdae"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Server URL 추가
        buildConfigField("String", "SERVER_URL", "\"${localProperties.getProperty("SERVER_URL", "")}\"")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewbinding)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit + okhttp3
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0") // 최신 버전 사용 가능

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutin 을 사용하기 위한 lib
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // 뷰모델
    implementation ("androidx.fragment:fragment-ktx:1.5.7")

    // Coil
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")

    // glide
    implementation ("com.github.bumptech.glide:glide:4.14.2")


    // CameraX 핵심 라이브러리
    implementation ("androidx.camera:camera-core:1.3.0")

    // CameraX - Camera2 기본 구현 (이게 없어서 오류 발생!)
    implementation ("androidx.camera:camera-camera2:1.3.0")

    // CameraX - 생명주기 처리 (Activity/Fragment에서 사용 가능)
    implementation ("androidx.camera:camera-lifecycle:1.3.0")

    // CameraX - 미리보기
    implementation ("androidx.camera:camera-view:1.3.0")

    // Material
    implementation ("com.google.android.material:material:1.10.0")

    // CustomTabsIntent
    implementation ("androidx.browser:browser:1.5.0")

    // EncryptedSharedPreferences
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    // QR scanning
    implementation ("com.google.mlkit:barcode-scanning:17.1.0")

    // Progress Bar
    implementation("com.beardedhen:androidbootstrap:2.3.2") {
        exclude(group = "com.android.support")
    }

    //포토뷰
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")

    // Lottie
    implementation ("com.airbnb.android:lottie:5.2.0")

    // 바텀 시트
    implementation("com.google.android.material:material:1.2.1")

    // hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
}