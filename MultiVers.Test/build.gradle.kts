import org.gradle.toolchains.foojay.match

plugins {
    kotlin("jvm")
    id("shoebox.multivers") version "1.0.0.124"
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
        match(".*")
        exclude(".*[a-zA-z].*")

        range("0", "") {
            exclude()
        }
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
