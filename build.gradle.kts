// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        google()
        maven {
            url = uri("https://repo.eclipse.org/content/repositories/paho-releases/")
        }
        maven {
            url = uri("https://github.com/alibaba/fastjson.git")
        }
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:4.0.2")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20-M1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }

}

allprojects {
    repositories {
        jcenter()
        google()
    }
}

tasks.register("clean", Delete::class) {
        delete(rootProject.buildDir)
}
