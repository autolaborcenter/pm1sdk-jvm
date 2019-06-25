plugins {
    java
    kotlin("jvm") version "1.3.40"
}

group = "cn.autolabor"
version = "v0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.java.dev.jna", "jna", "+")
    testCompile("junit", "junit", "+")
}
