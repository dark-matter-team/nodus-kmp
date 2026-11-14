import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)

    implementation(libs.compose.uiToolingPreview)
}

// OAuth-клієнт Google типу "Desktop app" з local.properties (не в git).
// Без ключів збірка проходить, а Google-вхід показує, що він не налаштований.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val googleDesktopClientId: String = localProperties.getProperty("google.desktopClientId", "").trim()
val googleDesktopClientSecret: String = localProperties.getProperty("google.desktopClientSecret", "").trim()

if (googleDesktopClientId.isNotEmpty() &&
    !Regex("""^\d+-[a-z0-9]+\.apps\.googleusercontent\.com$""").matches(googleDesktopClientId)
) {
    throw GradleException(
        "local.properties: google.desktopClientId має бути Desktop client ID без схеми й пробілів, " +
            "у форматі <цифри>-<рядок>.apps.googleusercontent.com",
    )
}

compose.desktop {
    application {
        mainClass = "com.darkmatter.nodus.MainKt"

        jvmArgs += listOf(
            "-Dnodus.google.desktopClientId=$googleDesktopClientId",
            "-Dnodus.google.desktopClientSecret=$googleDesktopClientSecret",
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.darkmatter.nodus"
            packageVersion = "1.0.0"
            // Локальний сервер для повернення з входу Google (com.sun.net.httpserver)
            modules("jdk.httpserver")
        }
    }
}
