plugins {
    id("application")
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta6"
}

group = "com.artillexstudios.jarstubgenerator"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-cli:commons-cli:1.9.0")
    implementation("org.ow2.asm:asm:9.7.1")

}

application {
    mainClass = "com.artillexstudios.jarstubgenerator.Main"
}