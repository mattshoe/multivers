plugins {
    kotlin("jvm")
    id("shoebox.multivers") version "1.0.0.109"
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
        range("1.0.0", "2.0.0") {
            exclude(".*-RC.*")
            exclude(".*Beta")
            runGradleTasks("check")
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
