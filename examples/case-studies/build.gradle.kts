plugins {
    id("shared-android")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    defaultConfig {
        applicationId = "composablearchitecture.example.casestudies"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = kotlinComposeVersion
    }
}

dependencies {
    implementation("androidx.dynamicanimation:dynamicanimation:$androidxDynamicAnimationVersion")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
}
