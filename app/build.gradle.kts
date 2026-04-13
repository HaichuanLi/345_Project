import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("jacoco")
}

val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.soen345.ticketing.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soen345.ticketing.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SENDER_EMAIL", "\"${localProperties.getProperty("SENDER_EMAIL") ?: ""}\"")
        buildConfigField("String", "GOOGLE_APP_PASSWORD", "\"${localProperties.getProperty("GOOGLE_APP_PASSWORD") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.extensions.configure<org.gradle.testing.jacoco.plugins.JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
}

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoTestReport") {

    dependsOn("testDebugUnitTest", ":core:test")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*"
    )

    val javaClasses = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")) {
        exclude(fileFilter)
    }

    val coreClasses = fileTree("${project(":core").buildDir}/classes/java/main") {
        exclude(fileFilter)
    }

    classDirectories.setFrom(files(javaClasses, coreClasses))
    sourceDirectories.setFrom(files(
        "src/main/java",
        "${project(":core").projectDir}/src/main/java"
    ))
    executionData.setFrom(
        layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"),
        file("${project(":core").buildDir}/jacoco/test.exec")
    )
}
