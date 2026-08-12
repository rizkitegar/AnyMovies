package com.movies.anymovies.core.database.ttl

import kotlin.time.Duration

public class TtlPolicy(
    private val storedAtMillis: Long,
    private val ttl: Duration,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    public fun isStale(): Boolean = nowMillis() - storedAtMillis >= ttl.inWholeMilliseconds
}
