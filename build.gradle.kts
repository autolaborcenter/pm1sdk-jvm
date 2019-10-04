plugins {
    kotlin("jvm") version "1.3.50"
}

group = "cn.autolabor"
version = "v0.0.3"

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
    .forEach { task -> task.exclude { "win" in it.path || "linux" in it.path } }
