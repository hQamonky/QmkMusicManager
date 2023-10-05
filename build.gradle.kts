
val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val swaggerVersion: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.4"
}

group = "com.music-manager.qmk"
version = "0.0.1"

application {
    mainClass.set("com.music-manager.qmk.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    // Ktor
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-data-rest:$swaggerVersion")
    implementation("org.springdoc:springdoc-openapi-ui:$swaggerVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$swaggerVersion")

    // Tools
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("net.jthink:jaudiotagger:3.0.1")
}
