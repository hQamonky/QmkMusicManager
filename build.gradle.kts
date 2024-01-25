
val qmkMusicManagerVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project
val coroutinesVersion: String by project
val logbackVersion: String by project
val swaggerVersion: String by project
val exposedVersion: String by project
val h2Version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.4"
}

group = "com.music-manager.qmk"
version = qmkMusicManagerVersion

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

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.h2database:h2:$h2Version")

    // Swagger
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-openapi:$ktorVersion")
    // Old spring swagger dependencies, remove when implemented for Ktor
    implementation("org.springdoc:springdoc-openapi-data-rest:$swaggerVersion")
    implementation("org.springdoc:springdoc-openapi-ui:$swaggerVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$swaggerVersion")

    // OkHttp
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.11.0"))
    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // Tests
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    // Tools
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("net.jthink:jaudiotagger:3.0.1")
    testImplementation("org.testng:testng:7.1.0")
}

tasks.named<JavaExec>("run") {
    systemProperty("qmkMusicManagerVersion", findProperty("qmkMusicManagerVersion") ?: "Unknown")
}

tasks.named<JavaExec>("run") {
    systemProperty("appName", findProperty("appName") ?: "QmkMusicManager")
}
