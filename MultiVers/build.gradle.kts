plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "io.github.mattshoe.shoebox"
version = "1.0.0.108"

repositories {
    mavenLocal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("MultiVers") {
            id = "shoebox.multivers"
            implementationClass = "io.github.mattshoe.shoebox.MultiVersPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-serialization:2.3.12")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.apache.maven:maven-artifact:3.9.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}