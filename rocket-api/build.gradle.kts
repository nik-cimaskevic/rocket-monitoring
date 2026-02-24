plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.7.0"
}

group = "com.rocket"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Observability
    implementation("io.micrometer:micrometer-registry-prometheus")

    // OpenAPI Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")

    // Structured JSON Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // UUID
    implementation("com.github.f4b6a3:uuid-creator:6.0.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.40")
    annotationProcessor("org.projectlombok:lombok:1.18.40")
    testCompileOnly("org.projectlombok:lombok:1.18.40")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.40")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/openapi/rocket-api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated-sources").map { it.asFile.path })
    apiPackage.set("com.rocket.api.openapi.api")
    modelPackage.set("com.rocket.api.openapi.model")
    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "useJakartaEe" to "true",
        "useBeanValidation" to "true",
        "dateLibrary" to "java8",
        "openApiNullable" to "false",
        "useTags" to "true",
        "skipDefaultInterface" to "true"
    ))
    typeMappings.set(mapOf("DateTime" to "java.time.ZonedDateTime"))
    importMappings.set(mapOf("java.time.ZonedDateTime" to "java.time.ZonedDateTime"))
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated-sources/src/main/java"))
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}
