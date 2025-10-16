import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.koin.android.compat)
            implementation(libs.koin.androidx.compose)
            implementation("io.ktor:ktor-client-okhttp:2.3.5")
            implementation("com.itextpdf:itext7-core:7.2.5")
            implementation("com.android.billingclient:billing-ktx:6.0.1")
        }
        commonMain.dependencies {
            implementation("media.kamel:kamel-image:0.9.5")
            implementation("io.ktor:ktor-client-core:2.3.5")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Database
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
// Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
// DateTime
            implementation(libs.kotlinx.datetime)
// Navigation
            implementation(libs.navigation.compose)
// Settings
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.koin.compose.viewmodel)
            implementation(compose.materialIconsExtended)
            api(libs.kmpnotifier)
        }
        iosMain.dependencies {
            implementation(libs.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.betofly.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.betofly.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file(project.property("KEYSTORE_FILE") as String)
            storePassword = project.property("KEYSTORE_PASSWORD") as String
            keyAlias = project.property("KEY_ALIAS") as String
            keyPassword = project.property("KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

sqldelight {
    databases {
        create("Betofly") {
            packageName.set("org.betofly.app.data")
            srcDirs("src/commonMain/kotlin")
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

