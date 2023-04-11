plugins {
    kotlin("plugin.serialization") version "1.8.10"
    id("com.android.application")
    kotlin("android")
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.2.5"
    id("kotlin-kapt")
    id("org.jetbrains.dokka")
}

android {
    buildFeatures {
        compose = true
    }
    compileSdk = 33
    defaultConfig {
        multiDexEnabled = true
        applicationId = "com.iomt.android"
        targetSdk = 33
        minSdk = 23
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            multiDexEnabled = true
        }
        getByName("debug") {
            multiDexEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    buildToolsVersion = "33.0.2"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}



dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.ui:ui:1.4.1")
    implementation("androidx.compose.runtime:runtime:1.4.1")
    implementation("androidx.compose.compiler:compiler:1.4.4")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.1")
    implementation("io.ktor:ktor-client-logging-jvm:2.2.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.1")

    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")

    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
    implementation("androidx.room:room-paging:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")

    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.credentials:credentials:1.2.0-alpha02")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0-alpha02")

    implementation("com.google.android.material:material:1.8.0")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1") { exclude("support-v4") }

    implementation("com.akuleshov7:ktoml-core:0.4.1")
    implementation("com.akuleshov7:ktoml-file:0.4.1")

    implementation("io.ktor:ktor-client-core:2.2.4")
    implementation("io.ktor:ktor-client-auth:2.2.4")
    implementation("io.ktor:ktor-client-android:2.2.4")
    implementation("io.ktor:ktor-client-logging:2.2.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("io.ktor:ktor-client-mock:2.2.4")
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    dokkaPlugin("org.jetbrains.dokka:android-documentation-plugin:1.8.10")
}

configurations.all {
    resolutionStrategy { force("androidx.core:core-ktx:1.6.0") }
}

diktat {
    diktatConfigFile = rootProject.file("diktat-analysis.yml")
    inputs {
        include("src/**/*.kt")
        exclude("src/test/**", "src/androidTest/**")
    }
    debug = false
}

kapt {
    showProcessorStats = true
}
