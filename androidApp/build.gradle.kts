import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

// Web client ID для Google-входу береться з local.properties (не в git).
// Без ключа збірка проходить, а Google-вхід показує, що він не налаштований.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val googleWebClientId: String = localProperties.getProperty("google.webClientId", "").trim()

// Неправильний формат (наприклад, з "https://") Google відхиляє з "[28444] Developer console is not set up correctly",
// тому зупиняємо збірку одразу з зрозумілим поясненням
if (googleWebClientId.isNotEmpty() &&
    !Regex("""^\d+-[a-z0-9]+\.apps\.googleusercontent\.com$""").matches(googleWebClientId)
) {
    throw GradleException(
        "local.properties: google.webClientId має бути саме Web client ID без схеми й пробілів, " +
            "у форматі <цифри>-<рядок>.apps.googleusercontent.com",
    )
}

// Адреса бекенда: debug ходить на локальний бекенд (10.0.2.2 це комп'ютер для емулятора),
// release на прод з local.properties (api.prodBaseUrl), поки прод не розгорнуто (KNU-63)
val apiDebugBaseUrl = "http://10.0.2.2:5033"
val apiProdBaseUrl: String = localProperties.getProperty("api.prodBaseUrl", "").trim()

if (apiProdBaseUrl.isNotEmpty() && !apiProdBaseUrl.startsWith("https://")) {
    throw GradleException("local.properties: api.prodBaseUrl має починатися з https://")
}

// Release без прод-адреси ходив би на локальну адресу, тож таку збірку зупиняємо одразу
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    // Локальна копія: дія задачі не може посилатися на об'єкт скрипта через кеш конфігурації
    val prodBaseUrl = apiProdBaseUrl
    doFirst {
        if (prodBaseUrl.isEmpty()) {
            throw GradleException("Для release-збірки задайте api.prodBaseUrl у local.properties")
        }
    }
}

android {
    namespace = "com.darkmatter.nodus"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.darkmatter.nodus"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$apiDebugBaseUrl\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$apiProdBaseUrl\"")
            isMinifyEnabled = false
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}