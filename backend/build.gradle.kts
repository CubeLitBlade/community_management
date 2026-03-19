plugins {
    id("java")
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.cubelitblade"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Temporary overrides for vulnerable transitive dependencies.
// These versions will be removed once Spring Boot updates its BOM.
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.3")
    }
    dependencies {
        dependency("tools.jackson.core:jackson-core:3.1.0") // fixes CVE-2025-52999
        dependency("org.assertj:assertj-core:3.27.7") // fixes CWE-611 related issue
        dependency("org.apache.commons:commons-lang3:3.20.0")
        dependency("ch.qos.logback:logback-core:1.5.32")
    }
}


dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    compileOnly("org.projectlombok:lombok:1.18.42")
    implementation ("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation ("com.baomidou:mybatis-plus-spring-boot4-starter:3.5.16")
    implementation("org.postgresql:postgresql")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.mockito:mockito-core:5.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose:4.1.0-M2")
}

tasks.test {
    useJUnitPlatform()
}