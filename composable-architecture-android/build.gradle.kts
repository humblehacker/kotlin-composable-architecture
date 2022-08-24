plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    `maven-publish`
}

android {
    compileSdk = androidCompileSdkVersion
    sourceSets["main"].java.srcDir("src/main/kotlin")
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = kotlinComposeCompilerVersion
    }
    namespace = "composablearchitecture.android"
    defaultConfig {
        aarMetadata {
            minCompileSdk = androidCompileSdkVersion
            minSdk = androidMinSdkVersion
        }
    }
    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
            allVariants()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.humblehacker"
            artifactId = "composable-architecture-android"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$androidxLifecycleVersion")

    api(project(":composable-architecture"))

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:$kotlinComposeVersion")
    implementation("androidx.compose.material:material:$kotlinComposeVersion")
    implementation("androidx.navigation:navigation-compose:$androidxNavigationVersion")
    implementation("androidx.navigation:navigation-runtime-ktx:$androidxNavigationVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$kotlinComposeVersion")
}
