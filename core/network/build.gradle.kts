import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun tmdbApiKey(): String {
    return localProperties.getProperty("TMDB_API_KEY")
        ?: (project.findProperty("TMDB_API_KEY") as? String)
        ?: ""
}

android {
    namespace = "com.movies.anymovies.core.network"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "TMDB_API_KEY", "\"${tmdbApiKey()}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    api(project(":core:common"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    api(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    api(libs.okhttp.core)
    debugImplementation(libs.okhttp.logging.interceptor)

    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
