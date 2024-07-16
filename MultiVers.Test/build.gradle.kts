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

    dependency("com.foo.bar") {
        range("1.0.0", "2.0.0")
    }

    dependency("io.derp.flerp:FlerpDerp") {
        match("^1\\..*") {
            runGradleTasks("detekt")
        }
        exclude(".*[a-zA-z].*")
    }

    dependency("some.dep.foo:SomeDep") {
        version("1.2.3")
    }

    dependency("another.foo:AnotherDep") {
        version("4.2.3") {
            runGradleTasks("lint")
        }
        range("3.1.2", "4.0.0") {
            exclude("3\\.2.*")
            runGradleTasks("check", "compile")
        }
    }

    dependency("com.this.is.dep:TheDep") {
        version("")
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
