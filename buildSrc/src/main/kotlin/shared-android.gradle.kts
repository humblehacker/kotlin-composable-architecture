plugins {
    id("com.android.application")
    kotlin("android")
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = androidCompileSdkVersion

    defaultConfig {
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isDebuggable = true
        }
    }

    @Suppress("UnstableApiUsage")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
}

dependencies {
    androidTestImplementation("androidx.test.espresso:espresso-core:$androidxEspressoVersion")
    androidTestImplementation("androidx.test.ext:junit:$androidxJunitVersion")
    implementation("androidx.activity:activity-ktx:$androidxActivityVersion")
    implementation("androidx.appcompat:appcompat:$androidxAppcompatVersion")
    implementation("androidx.constraintlayout:constraintlayout:$androidxConstraintLayoutVersion")
    implementation("androidx.core:core-ktx:$androidxCoreVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$androidxLifecycleVersion")
    implementation("androidx.recyclerview:recyclerview:$androidxRecyclerviewVersion")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:$androidxActivityVersion")
    implementation("androidx.compose.foundation:foundation:$kotlinComposeVersion")
    implementation("androidx.compose.material:material:$kotlinComposeVersion")
    implementation("androidx.compose.ui:ui-tooling:$kotlinComposeVersion")
    implementation("androidx.compose.ui:ui:$kotlinComposeVersion")
    implementation("androidx.navigation:navigation-compose:$androidxNavigationVersion")
    implementation("androidx.navigation:navigation-runtime-ktx:$androidxNavigationVersion")

    implementation(project(":composable-architecture"))
    implementation(project(":composable-architecture-android"))

    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation(project(":composable-architecture-test"))
}
