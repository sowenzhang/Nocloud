import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.3"
}

group = "com.nocloudchat"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    // Compose Desktop (includes UI, Foundation, Graphics)
    implementation(compose.desktop.currentOs)

    // Material3 for Compose
    implementation(compose.material3)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")

    // JSON (lightweight, zero transitive deps)
    implementation("org.json:json:20240303")
}

compose.desktop {
    application {
        mainClass = "com.nocloudchat.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NoCloud Chat"
            packageVersion = "1.0.0"
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

kotlin {
    jvmToolchain(21)
}

// ─── Icon generator ───────────────────────────────────────────────────────────
// Run once (or after design changes): ./gradlew generateIcon
tasks.register<JavaExec>("generateIcon") {
    group       = "tools"
    description = "Generates app icons into icons/ and src/main/resources/"
    classpath   = sourceSets["main"].runtimeClasspath
    mainClass.set("com.nocloudchat.tools.GenerateIconKt")
    dependsOn("compileKotlin")
    workingDir  = projectDir
}

