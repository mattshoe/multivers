plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "io.github.mattshoe.shoebox"
version = "1.0.0.9"

repositories {
    mavenLocal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("MultiVers") {
            id = "shoebox-multivers"
            implementationClass = "io.github.mattshoe.shoebox.MultiVersPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}