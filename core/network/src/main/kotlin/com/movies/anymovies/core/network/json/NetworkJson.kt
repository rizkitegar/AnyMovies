package com.movies.anymovies.core.network.json

import kotlinx.serialization.json.Json

public val networkJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}
