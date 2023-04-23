
// Top-level build file where you can add configuration options common to all subprojects/modules.

group = "com.iomt"

buildscript {
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://repo.eclipse.org/content/repositories/paho-releases/")
        }
    }
    dependencies {
        classpath("com.android.tools:r8:8.0.40")
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin") {
            version {
                strictly("1.8.10")
            }
        }
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.2.5"
}

diktat {
    diktatConfigFile = rootProject.file("diktat-analysis.yml")
    githubActions = findProperty("diktat.githubActions")?.toString()?.toBoolean() ?: false
    inputs {
        include("src/**/*.kt", "*.kts", "src/**/*.kts")
        exclude("build/**", "src/test/**", "src/androidTest/**")
    }
}
