pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AnyMovies"
include(":app")
include(":build-logic")

include(":core:common")
include(":core:network")
include(":core:database")
include(":core:designsystem")
include(":core:ui")
include(":core:ui-legacy")
include(":core:analytics")
include(":core:testing")

include(":navigation")

include(":feature:genre:domain")
include(":feature:genre:data")
include(":feature:genre:presentation")

include(":feature:movies:domain")
include(":feature:movies:data")
include(":feature:movies:presentation")

include(":feature:detail:domain")
include(":feature:detail:data")
include(":feature:detail:presentation")

include(":benchmark")
include(":baselineprofile")
