package com.movies.anymovies.core.network.json

import kotlinx.serialization.json.Json

val networkJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}
