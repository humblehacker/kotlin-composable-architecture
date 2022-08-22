plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    `maven-publish`
}

android {
    compileSdk = androidCompileSdkVersion
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }
    namespace = "composablearchitecture.android"
    defaultConfig {
        aarMetadata {
            minCompileSdk = androidCompileSdkVersion
            minSdk = androidMinSdkVersion
        }
    }
    flavorDimensions += "ext"
    productFlavors {
        create("default") {
            dimension = "ext"
        }
        create("arrow") {
            dimension = "ext"
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
                from(components["defaultRelease"])
            }
        }
        register<MavenPublication>("arrow") {
            groupId = "com.humblehacker"
            artifactId = "composable-architecture-android-arrow"

            afterEvaluate {
                from(components["arrowRelease"])
            }
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$androidxLifecycleVersion")

    "arrowApi"(project(":composable-architecture")) {
        capabilities {
            requireCapability("composable-architecture:composable-architecture-arrow:$arrowVersion")
        }
    }
    "defaultApi"(project(":composable-architecture"))

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:$kotlinComposeVersion")
    implementation("androidx.compose.material:material:$kotlinComposeVersion")
    implementation("androidx.navigation:navigation-compose:$androidxNavigationVersion")
    implementation("androidx.navigation:navigation-runtime-ktx:$androidxNavigationVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$kotlinComposeVersion")
}
