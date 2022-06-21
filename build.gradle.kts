import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

application {
    mainClass.set("no.nav.dagpenger.dokumentinnsending.AppKt")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<Jar>().configureEach {
    dependsOn("test")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }

    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}

dependencies {
    implementation(RapidAndRiversKtor2)

    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)

    implementation(Ktor2.Server.library("auth"))
    implementation(Ktor2.Server.library("auth-jwt"))
    implementation(Ktor2.Server.library("core"))
    implementation(Ktor2.Server.library("content-negotiation"))
    implementation(Ktor2.Server.library("cio"))
    implementation(Ktor2.Server.library("default-headers"))
    implementation(Ktor2.Server.library("metrics-micrometer"))
    implementation(Ktor2.Server.library("status-pages"))
    implementation("io.ktor:ktor-serialization-jackson:${Ktor2.version}")
    implementation("com.github.navikt.dp-biblioteker:oauth2-klient:2022.06.20-08.17.4f9161a85b76")
    // implementation(Ktor2.Server.library("call-id"))
    // implementation(Ktor2.Server.library("call-logging"))
    // implementation(Ktor2.Server.library("compression"))
    implementation(Ktor2.Client.library("cio"))
    implementation(Ktor2.Client.library("content-negotiation"))
    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Kotlinquery)
    implementation(Database.Postgres)

    testImplementation(TestContainers.postgresql)
    testImplementation("no.nav.security:mock-oauth2-server:0.5.1")
    testImplementation(kotlin("test"))
    testImplementation(Ktor2.Server.library("test-host"))
    // testImplementation(Ktor2.Client.library("mock"))
    testImplementation(Mockk.mockk)
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
