plugins {
    id("kotlin")
    `maven-publish`
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.humblehacker"
            artifactId = "composable-architecture"

            afterEvaluate {
                from(components["kotlin"])
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation(project(":composable-architecture-test"))
}
