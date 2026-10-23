plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "dev.whaltermc.veyra"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.whaltermc.veyra"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,LICENSE.txt,license.txt,NOTICE,NOTICE.txt}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // Termux build configuration - SDK path from local.properties
    // aapt2 binary substituted by Termux's aapt2
}

dependencies {
    // Core Android
    val core_ktx_version = "1.13.1"
    val lifecycle_version = "2.8.4"
    val activity_version = "1.9.2"
    val fragment_version = "1.8.2"

    implementation("androidx.core:core-ktx:$core_ktx_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.activity:activity-compose:$activity_version")
    implementation("androidx.fragment:fragment-ktx:$fragment_version")

    // Compose BOM
    val compose_bom_version = "2024.06.00"
    implementation(platform("androidx.compose:compose-bom:$compose_bom_version"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime-rxjava3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // DataStore (Preferences + Proto)
    val datastore_version = "1.1.1"
    implementation("androidx.datastore:datastore-preferences:$datastore_version")
    implementation("androidx.datastore:datastore-core:$datastore_version")

    // Security / EncryptedSharedPreferences
    val security_version = "1.1.0-alpha06"
    implementation("androidx.security:security-crypto:$security_version")

    // Networking
    val okhttp_version = "4.12.0"
    val retrofit_version = "2.11.0"
    val moshi_version = "1.15.1"
    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttp_version")
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit_version")
    implementation("com.squareup.moshi:moshi-kotlin:$moshi_version")
    implementation("com.squareup.moshi:moshi-adapters:$moshi_version")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshi_version")

    // Image Loading
    val coil_version = "2.6.0"
    implementation("io.coil-kt:coil-compose:$coil_version")

    // Coroutines & Flow
    val coroutines_version = "1.8.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines_version")

    // Serialization
    val serialization_version = "1.6.3"
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serialization_version")

    // Zip/Archive handling
    implementation("org.apache.commons:commons-compress:1.26.1")

    // WorkManager (background tasks)
    val work_version = "2.9.1"
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-data")
}