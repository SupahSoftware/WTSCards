import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.sqldelight.driver.jvm)
            implementation(libs.sqldelight.coroutines)
        }
    }
}

sqldelight {
    databases {
        create("WTSCardsDatabase") {
            packageName.set("com.wtscards.db")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}


compose.desktop {
    application {
        mainClass = "com.wtscards.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "WTSCards"
            packageVersion = project.findProperty("app.version") as String? ?: "1.0.0"
            description = "WTS Cards Application"
            vendor = "WTSCards"

            windows {
                menuGroup = "WTSCards"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            }

            macOS {
                bundleID = "com.wtscards"
            }

            linux {
            }
        }
    }
}
