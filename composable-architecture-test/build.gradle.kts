plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    implementation(project(":composable-architecture"))
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.humblehacker"
            artifactId = "composable-architecture-test"

            afterEvaluate {
                from(components["kotlin"])
            }
        }
    }
}

