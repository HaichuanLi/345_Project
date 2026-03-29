buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("com.google.gms:google-services:4.4.4")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    tasks.withType<Test>().configureEach {
        testLogging {
            events("passed", "failed", "skipped")
            showExceptions = false
            showCauses = false
            showStackTraces = false
        }
    }
}