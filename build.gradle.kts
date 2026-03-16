plugins {
    id("com.android.application") version "8.5.2" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
