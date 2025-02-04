plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "hasura"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.cli)
    implementation(libs.vertx.core)
    implementation(libs.vertx.web)
    implementation(libs.vertx.kotlin)
    implementation(libs.vertx.kotlin.coroutines)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.semver4j)
    
    // Micrometer
    implementation("io.micrometer:micrometer-core:1.11.3")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.3")
    
    // OpenTelemetry
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.kotlin)
    implementation(libs.opentelemetry.semconv)
    implementation(libs.opentelemetry.otlp)
}

tasks.shadowJar {
}
