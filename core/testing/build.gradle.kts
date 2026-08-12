plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.movies.anymovies.core.testing"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.test)
    implementation(libs.koin.test.junit4)
    implementation(libs.junit)
}
