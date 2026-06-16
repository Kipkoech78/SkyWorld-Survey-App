plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // id("com.google.dagger.hilt.android") version "2.57.1" apply false
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.skyworldsurveyapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.skyworldsurveyapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}


dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room3.external.antlr)
    implementation(libs.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    //dagger hilt dep
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.compose.material.icons.extended)



    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:5.4.0")
    //Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    //Datastore
    implementation ("androidx.datastore:datastore-preferences:1.2.1")

    //Compose Foundation
    implementation ("androidx.compose.foundation:foundation:1.11.2")

    //Accompanist
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    //Paging 3
    //def paging_version = ""
    implementation ("androidx.paging:paging-runtime:3.1.1")
    implementation ("androidx.paging:paging-compose:3.5.0")

// Networking (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation(libs.simple.xml)
    //implementation("com.squareup.retrofit2:converter-simplexml")

    implementation("com.squareup.retrofit2:converter-simplexml:3.0.0")

    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")

    //okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")

}