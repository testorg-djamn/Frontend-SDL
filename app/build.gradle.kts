plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
    id("org.sonarqube") version "5.1.0.4882"
}

jacoco {
    toolVersion = "0.8.8"
}

android {
    namespace = "at.aau.serg.sdlapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "at.aau.serg.sdlapp"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.useJUnitPlatform()
            }
        }
    }
}

// Stark vereinfachte JaCoCo-Konfiguration
tasks.register<JacocoReport>("generateTestCoverageReport") {
    group = "Reporting"
    description = "Generate Jacoco coverage reports after running tests."
    
    // Abhängigkeit von allen Unit-Tests
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    // Setze Klassenpfade für das Debug-Build
    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug"))
    classDirectories.setFrom(debugTree)
    
    // Quellverzeichnisse
    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    
    // Sammle alle verfügbaren Exec-Dateien
    executionData.setFrom(fileTree(project.layout.buildDirectory) {
        include("**/*.exec")
    })
}

sonar {
    properties {
        property("sonar.exclusions", "**/WheelActivity.kt")
        property("sonar.projectKey", "SE2-SS25-SpielDesLebens_Frontend-SDL")
        property("sonar.organization", "se2-ss25-spieldeslebens")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property("sonar.exclusions", "**/*Activity.kt,**/MyStomp.kt,**/Color.kt,**/Theme.kt,**/Type.kt,**/ActionCard.kt,**/PlayerModell.kt,**/PlayerRepository.kt,**/PlayerStatsOverlay.kt,**/GameScreen.kt,**/BoardData.kt,**/Field.kt,**/FieldTyp.kt,**Board.kt")

    }
}

dependencies {
    // App
    implementation(libs.krossbow.websocket.okhttp)
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.builtin)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.otaliastudios:zoomlayout:1.9.0")

    // Unit-Tests
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Instrumentation Tests (Espresso + Intents)
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")//

    // Compose Test
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}