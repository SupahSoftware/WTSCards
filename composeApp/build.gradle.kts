import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.wtscards.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "WTSCards"
            packageVersion = "1.0.0"
            description = "WTS Cards Application"
            vendor = "WTSCards"

            windows {
                menuGroup = "WTSCards"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                iconFile.set(project.file("icons/icon.ico"))
            }

            macOS {
                bundleID = "com.wtscards"
                iconFile.set(project.file("icons/icon.icns"))
            }

            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
        }
    }
}
