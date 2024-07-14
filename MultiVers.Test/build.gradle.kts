import io.github.mattshoe.shoebox.*
import io.github.mattshoe.shoebox.Variant

plugins {
    kotlin("jvm")
    id("shoebox-multivers") version "1.0.0.12"
}

group = "io.github.mattshoe.shoebox"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

multivers {
    runGradleTasksOnAllVariants("compile", "test")

    variant("io.github.mattshoe.shoebox:ShoeBoxData") {
        version("1.0.0")
        version("1.5.3") {
            runGradleTasks("build", "compile")
        }
        range("1.0.0", "2.0.0") {
            exclude(".*-SNAPSHOT")
            runGradleTasks("build", "test")
        }
    }
    variant("io.github.mattshoe.shoebox:AutoRepo") {
        range("0.0.1", "1.0.0") {
            exclude(".*-RC")
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