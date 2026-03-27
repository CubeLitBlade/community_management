plugins {
    id("java")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "io.github.cubelitblade"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    // Lombok
    annotationProcessor(libs.lombok)
    compileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)

    // Spring Boot
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)

    // MyBatis-Plus & PostgreSQL
    implementation(libs.mybatis.plus)
    implementation(libs.postgresql)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Development Only
    testAndDevelopmentOnly(libs.spring.boot.docker.compose)

    // Mockito Agent Configuration
    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

val mockitoAgentPath: Provider<String> = mockitoAgent.elements.map {
    it.single().asFile.absolutePath
}

tasks.test {
    useJUnitPlatform()

    jvmArgumentProviders.add(object : CommandLineArgumentProvider {
        @get:InputFile
        @get:PathSensitive(PathSensitivity.NONE)
        val agentFile = mockitoAgent.elements.map {
            it.single()
        }

        override fun asArguments(): Iterable<String> {
            return listOf("-javaagent:${agentFile.get().asFile.absolutePath}")
        }
    })

    jvmArgs("-Xshare:off")
}