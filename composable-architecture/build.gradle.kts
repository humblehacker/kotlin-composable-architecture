plugins {
    id("kotlin")
    id("kotlin-kapt")
}

dependencies {
    implementation("io.arrow-kt:arrow-optics:$arrowVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    kaptTest("io.arrow-kt:arrow-meta:$arrowVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation(project(":composable-architecture-test"))
}
