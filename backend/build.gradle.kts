/**
 * Das ist die Build-Datei für das Backend-Projekt.
 * Sie definiert die Plugins, Abhängigkeiten und andere Konfigurationen, die für den Bau und die Ausführung der
 * Java Spring Boot-Anwendung erforderlich sind.
 */
plugins {
    id("java")
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.5.0"
}

group = "io.github.mexikoedi"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

springBoot {
    buildInfo()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-h2console")
    runtimeOnly("com.h2database:h2:2.4.240")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
    val mockitoAgent = classpath.first { it.name.startsWith("mockito-core") }
    jvmArgs("-javaagent:${mockitoAgent.absolutePath}", "-Xshare:off")
}
