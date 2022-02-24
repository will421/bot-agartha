import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    kotlin("jvm")

    id("com.github.jakemarsden.git-hooks")
    id("com.github.johnrengelman.shadow")
    id("io.gitlab.arturbosch.detekt")
}

group = "fr.will421.botagartha"
version = "1.0-SNAPSHOT"


repositories {
    mavenLocal()


    maven { url = uri("https://jitpack.io") }
    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.kord.extensions)
//    implementation(libs.kord.core) {
//        version {
//            strictly("undefined")
//        }
//    }
    implementation(libs.kotlin.stdlib)
    implementation(libs.jackson.yaml)
    implementation(libs.jackson.kotlin)
    implementation(libs.vosk)
    implementation(libs.jna)
    implementation(libs.opus.java)
    implementation(libs.wavfile)

    // Logging dependencies
    implementation(libs.groovy)
    implementation(libs.logback)
    implementation(libs.logging)
}


application {
    // This is deprecated, but the Shadow plugin requires it
    @Suppress("DEPRECATION")
    mainClassName = "bot-agartha.AppKt"
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt")
    )
}

tasks.withType<KotlinCompile> {
    // Current LTS version of Java
    kotlinOptions.jvmTarget = "11"

    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "template.AppKt"
        )
    }
}

java {
    // Current LTS version of Java
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

detekt {
    buildUponDefaultConfig = true
    config = rootProject.files("detekt.yml")
}