plugins {
    kotlin("plugin.serialization") version "1.6.10"
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.2.1"
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.iomt.android"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}



dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.navigation:navigation-fragment:2.4.1")
    implementation("androidx.navigation:navigation-ui:2.4.1")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.5.0-alpha02")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1") {
        exclude("support-v4")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.alibaba:fastjson:1.2.61")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.akuleshov7:ktoml-core:0.2.11")
    implementation("com.akuleshov7:ktoml-file:0.2.11")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.20-M1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
//    testImplementation("junit:junit:5.7.1")
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy { force("androidx.core:core-ktx:1.6.0") }
}

diktat {
    inputs {
//        include("src/**/*.kt")
        include("src/**/SignupActivity.kt", "src/**/SenderService.kt", "src/**/Requests.kt")
        exclude("src/test/**", "src/androidTest/**")
    }
    debug = true
}
