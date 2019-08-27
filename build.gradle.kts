plugins {
    kotlin("jvm") version "1.3.50"
}

group = "cn.autolabor"
version = "v0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("net.java.dev.jna", "jna", "+")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

tasks
    .withType(Jar::class)
    .first()
    .exclude {
        "linux_x64" in it.path
        || "win_x64" in it.path
        || "win_x86" in it.path
    }
