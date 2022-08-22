plugins {
    idea
    id("kotlin")
    id("com.google.devtools.ksp")
    `maven-publish`
}

var arrow = sourceSets.create("arrow")

java {
    registerFeature("arrow") {
        usingSourceSet(arrow)
        capability(project.group.toString(), "${project.name}-arrow", project.version.toString())
    }
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

idea {
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    "arrowApi"("io.arrow-kt:arrow-optics:$arrowVersion")
    "arrowApi"(project(path))
    "arrowImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    ksp("io.arrow-kt:arrow-optics-ksp-plugin:$arrowVersion")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation(project(":composable-architecture-test"))
}
