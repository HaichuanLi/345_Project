import java.util.Properties

plugins {
    `java-library`
    jacoco
}

sourceSets {
    named("main") {
        java.setSrcDirs(listOf("../src/main/java"))
    }
    named("test") {
        java.setSrcDirs(listOf("../src/test/java"))
    }
}

// Logic to load properties from local.properties
val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val emailUser = localProperties.getProperty("SENDER_EMAIL") ?: ""
val emailPassword = localProperties.getProperty("GOOGLE_APP_PASSWORD") ?: ""

dependencies {
    implementation("jakarta.mail:jakarta.mail-api:2.1.5")
    runtimeOnly("org.eclipse.angus:angus-mail:2.0.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.testng:testng:6.9.6")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

// Since java-library doesn't have buildConfigField, we can pass these as system properties to tests
// or generate a resource file. For tests, system properties are easiest.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("EMAIL_USER", emailUser)
    systemProperty("EMAIL_PASSWORD", emailPassword)

    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = false
        showCauses = false
        showStackTraces = false
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}