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
        release {
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