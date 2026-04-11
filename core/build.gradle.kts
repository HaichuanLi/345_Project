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

dependencies {
    implementation("com.sun.mail:javax.mail:1.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.testng:testng:6.9.6")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
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