plugins {
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
}

group = "com.mindshift"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("commons-codec:commons-codec:1.16.0")

    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

    // Template Engines
    implementation("com.github.spullara.mustache.java:compiler:0.9.11")

    // HTTP Client
    implementation("org.springframework.boot:spring-boot-starter-webflux") // For WebClient
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Firebase Admin SDK for FCM
    implementation("com.google.firebase:firebase-admin:9.2.0")

    // AWS SDK for SES
    implementation("software.amazon.awssdk:ses:2.21.0")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Utilities
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.google.guava:guava:32.1.3-jre")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    archiveFileName.set("ums-service.jar")
}