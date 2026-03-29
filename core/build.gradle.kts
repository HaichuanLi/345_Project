plugins {
    `java-library`
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
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "failed", "skipped")
        showExceptions = false
        showCauses = false
        showStackTraces = false
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
