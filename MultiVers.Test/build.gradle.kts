import io.github.mattshoe.shoebox.*
import io.github.mattshoe.shoebox.Variant

plugins {
    kotlin("jvm")
    id("shoebox-multivers") version "1.0.0.9"
}

group = "io.github.mattshoe.shoebox"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

multiVers {
    variant("org.io.foo:DerpYourself") {
        version("1.0.0") {
            runGradleTasks("build", "compile")
        }
        range("1.0.0", "2.0.0") {
            runGradleTasks("build", "test")
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