@file:Suppress("HasPlatformType")

import java.util.Properties

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
}

private val properties = Properties()
    .apply { File(rootDir.parentFile, "gradle.properties").inputStream().use { load(it) } }

val androidToolsBuildVersion = properties.getProperty("androidToolsBuildVersion")
val kotlinVersion = properties.getProperty("kotlinVersion")
val kotlinKspVersion = properties.getProperty("kotlinKspVersion")

dependencies {
    implementation("com.android.tools.build:gradle:$androidToolsBuildVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:$kotlinKspVersion")
}
