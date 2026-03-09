import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform") version "2.0.21"
    id("com.android.application") version "8.2.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.3"
}

group = "com.nocloudchat"
val appVersion = System.getenv("APP_VERSION") ?: "1.0.0"
version = appVersion

kotlin {
    jvm("desktop")

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.json:json:20240303")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.3")
                implementation("androidx.core:core-ktx:1.13.1")
                // Force 16 KB page-size aligned version of graphics-path (fixes Android Studio warning)
                implementation("androidx.graphics:graphics-path:1.0.1")
            }
        }
    }
}

android {
    namespace = "com.nocloudchat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nocloudchat.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = appVersion
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
            // KMP manages Kotlin sources via androidMain/commonMain — exclude legacy src/main/kotlin
            // to prevent duplicate class declarations
            java.setSrcDirs(listOf<File>())
            kotlin.setSrcDirs(listOf<File>())
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.nocloudchat.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NoCloud Chat"
            packageVersion = appVersion
            description = "Private home chat — no internet, no accounts, no cloud"
            copyright = "© 2026 NoCloud Chat"

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "NoCloud Chat"
                upgradeUuid = "a3b4c5d6-e7f8-4a5b-9c0d-1e2f3a4b5c6d"
            }
            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
            macOS {
                bundleID = "com.nocloudchat.app"
            }
        }
    }
}

// ─── Icon generator ───────────────────────────────────────────────────────────
tasks.register<JavaExec>("generateIcon") {
    group       = "tools"
    description = "Generates app icons into icons/ and src/main/resources/"
    dependsOn("compileDesktopMainKotlin")
    val compilation = (kotlin.targets.getByName("desktop") as KotlinJvmTarget)
        .compilations.getByName("main")
    classpath   = compilation.output.allOutputs + compilation.runtimeDependencyFiles
    mainClass.set("com.nocloudchat.tools.GenerateIconKt")
    workingDir  = projectDir
}
