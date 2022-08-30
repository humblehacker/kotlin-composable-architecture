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
        kotlinCompilerExtensionVersion = kotlinComposeCompilerVersion
    }
    namespace = "composablearchitecture.example.casestudies"
}

dependencies {
    implementation("androidx.dynamicanimation:dynamicanimation:$androidxDynamicAnimationVersion")
    implementation("androidx.appcompat:appcompat:$androidxAppcompatVersion")
    // implementation("com.google.android.material:material:1.6.1")
    implementation("com.github.jeziellago:compose-markdown:0.3.0")
    implementation("androidx.compose.material:material-icons-extended:$kotlinComposeVersion")
    implementation("com.google.accompanist:accompanist-permissions:0.25.1")
}
