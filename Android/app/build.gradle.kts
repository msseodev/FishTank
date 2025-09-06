plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    kotlin("kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
}


android {
    namespace = "com.marine.fishtank"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.marine.fishtank"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SERVER_URL", "\"http://211.255.170.80:15483\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
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
        viewBinding = true
        compose = true
    }



    kapt {
        correctErrorTypes = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    implementation(project(":MpChartLib"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // LifeCycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ETC
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)
    implementation(libs.logger)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)

    // Accompanist - for compose
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.swiperefresh)

    // Coroutine
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // ExoPlayer
    implementation(libs.exoplayer)
    implementation(libs.exoplayer.rtsp)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.sandwich)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
}