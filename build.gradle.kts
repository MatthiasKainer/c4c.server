import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    application
}

group = "de.matthiaskainer.c4c"
version = "1.0"

application {
    mainClassName = "de.matthiaskainer.c4c.AppKt"
}

repositories {
    mavenCentral()
    jcenter()
}

val kotlinCoroutinesVersion = "1.4.2"

val ktorVersion  = "1.5.3"
val arrowVersion = "0.12.0"
val exposedVersion = "0.30.1"

val logbackVersion = "1.2.3"
val h2DatabaseVersion = "1.4.199"
val postgresVersion = "42.2.2"

val junitVersion = "5.5.1"
val spekVersion  = "2.0.5"

val jacksonVersion = "2.12.3"

val loggingVersion = "1.12.5"

dependencies {
    // Base
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging:$loggingVersion")

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")

    // Arrow
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // Database
    implementation("com.h2database:h2:$h2DatabaseVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")

    // Jackson
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // Unit tests
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
