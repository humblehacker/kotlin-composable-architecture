plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = androidCompileSdkVersion
    sourceSets["main"].java.srcDir("src/main/kotlin")
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$androidxLifecycleVersion")

    implementation("io.arrow-kt:arrow-optics:$arrowVersion")

    api(project(":composable-architecture"))

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:$kotlinComposeVersion")
    implementation("androidx.compose.material:material:$kotlinComposeVersion")
    implementation("androidx.navigation:navigation-compose:$androidxNavigationVersion")
    implementation("androidx.navigation:navigation-runtime-ktx:$androidxNavigationVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$kotlinComposeVersion")
}
