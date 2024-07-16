import org.gradle.toolchains.foojay.match

plugins {
    kotlin("jvm")
    id("shoebox.multivers") version "1.0.0.114"
}

group = "io.github.mattshoe.shoebox"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

multivers {
    runGradleTasksOnAllVariants("test")

    dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC") {
        runGradleTasks("assemble")
        match(".*")
        exclude(".*[A-Z].*")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}
