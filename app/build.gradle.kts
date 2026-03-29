import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    FileInputStream(localPropsFile).use { input ->
        localProps.load(input)
    }
}

fun localProperty(name: String, defaultValue: String = ""): String {
    return localProps.getProperty(name, defaultValue)
}

fun escapeForBuildConfig(value: String): String {
    return value.replace("\\", "\\\\").replace("\"", "\\\"")
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

        buildConfigField("String", "SMTP_HOST", "\"${escapeForBuildConfig(localProperty("smtp.host"))}\"")
        buildConfigField("String", "SMTP_PORT", "\"${escapeForBuildConfig(localProperty("smtp.port", "587"))}\"")
        buildConfigField("String", "SMTP_USERNAME", "\"${escapeForBuildConfig(localProperty("smtp.username"))}\"")
        buildConfigField("String", "SMTP_PASSWORD", "\"${escapeForBuildConfig(localProperty("smtp.password"))}\"")
        buildConfigField("String", "SMTP_FROM", "\"${escapeForBuildConfig(localProperty("smtp.from"))}\"")
        buildConfigField("boolean", "SMTP_TLS", localProperty("smtp.tls", "true"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")
}
