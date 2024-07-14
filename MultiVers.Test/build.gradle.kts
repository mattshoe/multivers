plugins {
    kotlin("jvm")
    id("shoebox.multivers") version "1.0.0.92"
}

group = "io.github.mattshoe.shoebox"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

multivers {
    runGradleTasksOnAllVariants("test")

    dependency("io.github.mattshoe.shoebox.autobuilder:AutoBuilder.Processor") {
        runGradleTasks("assemble")
        range("0.0.0", "2.0.0") {
            exclude(".*-RC")
            runGradleTasks("check")
        }
    }

    dependency("io.github.mattshoe:shoebox-data") {
        version("0.0.3") {
            runGradleTasks("assemble")
        }
        range("0.0.0", "1.0.0") {
            exclude(".*-SNAPSHOT")
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
