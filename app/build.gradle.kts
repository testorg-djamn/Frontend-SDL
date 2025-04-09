plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
    id("org.sonarqube") version "5.1.0.4882"
}

jacoco {
    toolVersion = "0.8.8" // Ältere Version mit besserer Java-Kompatibilität
}

android {
    namespace = "at.aau.serg.websocketbrokerdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "at.aau.serg.websocketbrokerdemo"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
            all { test ->
                test.useJUnitPlatform()
                test.systemProperty("robolectric.logging", "stdout")
                test.systemProperty("robolectric.graphicsMode", "NATIVE")
                test.finalizedBy(tasks.named("jacocoTestReport"))
            }
        }
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generiert einen Code Coverage Report für Unit-Tests"
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/di/**/*.*",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/*_Provide*Factory.*",
        "**/*_ViewBinding.*"
    )

    val debugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(fileFilter)
    }

    val javaDebugTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug")) {
        exclude(fileFilter)
    }

    val mainSrc = listOf("${project.projectDir}/src/main/java", "${project.projectDir}/src/main/kotlin")

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree, javaDebugTree))
    executionData.setFrom(files(
        "${layout.buildDirectory.get()}/jacoco/testDebugUnitTest.exec",
        "${layout.buildDirectory.get()}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
    ))
}

// Garantieren, dass der Report nach der Test-Ausführung erstellt wird
tasks.withType<Test> {
    finalizedBy(tasks.named("jacocoTestReport"))
}

sonar {
    properties {
        property("sonar.projectKey", "SE2-SS25-SpielDesLebens_Frontend-SDL")
        property("sonar.organization", "se2-ss25-spieldeslebens")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property("sonar.exclusions", "**/ActionCardActivity.kt, **/ActionCard.kt")
    }
}

dependencies {
    // Core App Dependencies
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

    // Unit-Test (JVM) Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0")


    // Instrumentation Tests (laufen nicht bei Sonar)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}