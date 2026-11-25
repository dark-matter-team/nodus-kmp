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

// Адреса бекенда з local.properties (api.baseUrl), за замовчуванням локальний бекенд
val apiBaseUrl: String = localProperties.getProperty("api.baseUrl", "http://localhost:5033").trim()

if (!Regex("""^https?://\S+$""").matches(apiBaseUrl)) {
    throw GradleException("local.properties: api.baseUrl має бути адресою з http:// або https://")
}

val desktopJvmArgs = listOf(
    "-Dnodus.google.desktopClientId=$googleDesktopClientId",
    "-Dnodus.google.desktopClientSecret=$googleDesktopClientSecret",
    "-Dnodus.api.baseUrl=$apiBaseUrl",
)

// jvmArgs з compose.desktop отримують лише run і дистрибутив, а запуск з IDE йде через hotRun
// (Compose Hot Reload), тому йому їх передаємо окремо.
tasks.withType<JavaExec>().matching { it.name == "hotRun" }.configureEach {
    jvmArgs(desktopJvmArgs)
}

compose.desktop {
    application {
        mainClass = "com.darkmatter.nodus.MainKt"

        jvmArgs += desktopJvmArgs

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.darkmatter.nodus"
            packageVersion = "1.0.0"
            // Локальний сервер для повернення з входу Google (com.sun.net.httpserver)
            modules("jdk.httpserver")
        }
    }
}
