import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.android.application)
    id("de.mannodermaus.android-junit5") version "1.9.3.0" // Atualizado para versão mais recente
}

android {
    namespace = "com.senai.audioplayerservice"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.senai.audioplayerservice"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"

        externalNativeBuild {
            cmake {
                cppFlags.add("-std=c++11")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    ndkVersion = "28.0.13004108"

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                events = setOf(
                    TestLogEvent.FAILED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.PASSED
                )
            }
        }
    }
}

dependencies {

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)

        // MediaSessionCompat
        implementation("androidx.media:media:1.6.0")

        // =================================
        // Testes unitários (JVM / test/)
        // =================================
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.robolectric:robolectric:4.12.1")
        testImplementation("org.mockito:mockito-core:5.4.0")
        testImplementation("org.mockito:mockito-inline:5.4.0")
        testImplementation("org.mockito:mockito-junit-jupiter:5.4.0")

        // ===================================
        // Testes instrumentados (Android / androidTest/)
        // ===================================
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        androidTestImplementation("org.mockito:mockito-android:5.12.0")
        androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
        androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
}

