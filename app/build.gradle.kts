plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.facedetection"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.facedetection"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation(libs.play.services.vision.common)
    //implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    //implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.viewpager2:viewpager2:1.1.0")

    //1 Use this dependency to bundle the model with your app
    implementation("com.google.mlkit:face-detection:16.1.6")
    //2 Use this dependency to use the dynamically downloaded model in Google Play Services
    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")

    //3 cameraX and sync project with gradle files
    var camerax_version = "1.3.0-alpha03"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-video:${camerax_version}")

    implementation("androidx.camera:camera-view:${camerax_version}")
    implementation("androidx.camera:camera-extensions:${camerax_version}")

    //4 Splash API
    implementation("androidx.core:core-splashscreen:1.0.0")

    // Firebase BOM and Firestore
    // Import the Firebase BoM
    //implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // When using the BoM, don't specify versions in Firebase dependencies
    //implementation("com.google.firebase:firebase-analytics")

    // Add the dependency for the Realtime Database library
    //implementation("com.google.firebase:firebase-database")

    // Declare the dependency for the Cloud Firestore library
    //implementation("com.google.firebase:firebase-firestore")
    //implementation("com.google.firebase:firebase-firestore-ktx")

    // Add for Preference Fragment
    var preference_version = "1.2.1"
    implementation("androidx.preference:preference-ktx:${preference_version}")
    implementation("androidx.preference:preference:${preference_version}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")

    // for video downloader
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.3.1")
    implementation("androidx.compose.ui:ui:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.0")
    implementation("androidx.compose.material:material:1.2.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.2.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.2.0")

    implementation("com.android.support.constraint:constraint-layout:1.0.2")

    // for youtube player
    //implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


}
