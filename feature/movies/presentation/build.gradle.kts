plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.movies.anymovies.feature.movies.presentation"
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
    implementation(project(":feature:movies:domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:ui-legacy"))
    implementation(project(":core:designsystem"))
    implementation(project(":navigation"))
    implementation(project(":core:common"))
}
