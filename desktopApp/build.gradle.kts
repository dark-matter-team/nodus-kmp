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

val googleDesktopJvmArgs = listOf(
    "-Dnodus.google.desktopClientId=$googleDesktopClientId",
    "-Dnodus.google.desktopClientSecret=$googleDesktopClientSecret",
)

// jvmArgs з compose.desktop отримують лише run і дистрибутив, а запуск з IDE йде через hotRun
// (Compose Hot Reload), тому йому ключі передаємо окремо.
tasks.withType<JavaExec>().matching { it.name == "hotRun" }.configureEach {
    jvmArgs(googleDesktopJvmArgs)
}

// Прод-адреса бекенда для запакованого застосунку (msi/dmg/deb). Запуск з IDE завжди ходить
// на локальний бекенд: main.kt вмикає прод лише в запакованому застосунку
val apiProdBaseUrl: String = localProperties.getProperty("api.prodBaseUrl", "").trim()

if (apiProdBaseUrl.isNotEmpty() && !apiProdBaseUrl.startsWith("https://")) {
    throw GradleException("local.properties: api.prodBaseUrl має починатися з https://")
}

// Запакований застосунок без прод-адреси ходив би на localhost, тож пакування зупиняємо одразу
tasks.matching { it.name.startsWith("package") || (it.name.startsWith("create") && it.name.endsWith("Distributable")) }
    .configureEach {
        // Локальна копія: дія задачі не може посилатися на об'єкт скрипта через кеш конфігурації
        val prodBaseUrl = apiProdBaseUrl
        doFirst {
            if (prodBaseUrl.isEmpty()) {
                throw GradleException("Для пакування застосунку задайте api.prodBaseUrl у local.properties")
            }
        }
    }

compose.desktop {
    application {
        mainClass = "com.darkmatter.nodus.MainKt"

        jvmArgs += googleDesktopJvmArgs
        jvmArgs += "-Dnodus.api.prodBaseUrl=$apiProdBaseUrl"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.darkmatter.nodus"
            packageVersion = "1.0.0"
            // Локальний сервер для повернення з входу Google (com.sun.net.httpserver)
            modules("jdk.httpserver")
        }
    }
}
