package com.movies.anymovies.core.database.ttl

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

class TtlPolicyTest {

    @Test
    fun `isStale returns false when age is below ttl`() {
        val policy = TtlPolicy(
            storedAtMillis = 0L,
            ttl = 10.minutes,
            nowMillis = { 5.minutes.inWholeMilliseconds },
        )

        assertFalse(policy.isStale())
    }

    @Test
    fun `isStale returns true when age equals ttl`() {
        val policy = TtlPolicy(
            storedAtMillis = 0L,
            ttl = 10.minutes,
            nowMillis = { 10.minutes.inWholeMilliseconds },
        )

        assertTrue(policy.isStale())
    }

    @Test
    fun `isStale returns true when age exceeds ttl`() {
        val policy = TtlPolicy(
            storedAtMillis = 0L,
            ttl = 10.minutes,
            nowMillis = { 15.minutes.inWholeMilliseconds },
        )

        assertTrue(policy.isStale())
    }
}
