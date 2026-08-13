package com.movies.anymovies.feature.detail.data.repository

import androidx.paging.testing.asSnapshot
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.network.json.networkJson
import com.movies.anymovies.feature.detail.data.remote.ReviewApi
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit

class ReviewRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ReviewApi
    private lateinit var repository: ReviewRepositoryImpl

    private val testDispatchers = object : CoroutineDispatchers {
        override val io = Dispatchers.Unconfined
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()

        val client = OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.MILLISECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
            .build()

        api = retrofit.create(ReviewApi::class.java)
        repository = ReviewRepositoryImpl(reviewApi = api, dispatchers = testDispatchers)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun reviewsResponseJson(page: Int, totalPages: Int, totalResults: Int, reviewIds: List<String>): String {
        val results = reviewIds.joinToString(",") { id ->
            """{"id":"$id","author":"author-$id","author_details":{"name":"Author $id",""" +
                """"username":"user$id","avatar_path":null,"rating":7.0},"content":"Review $id",""" +
                """"created_at":"2026-01-01T00:00:00.000Z"}"""
        }
        return """{"page":$page,"results":[$results],"total_pages":$totalPages,"total_results":$totalResults}"""
    }

    @Test
    fun `paging data emits page one items`() = runTest {
        server.enqueue(
            MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 1, totalResults = 2, reviewIds = listOf("a", "b"))),
        )

        val snapshot = repository.getReviewsPagingData(movieId = 1).asSnapshot()

        assertEquals(listOf("a", "b"), snapshot.map { it.id })
    }

    @Test
    fun `scrolling toward the end loads and appends the next page`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                reviewsResponseJson(page = 1, totalPages = 5, totalResults = 100, reviewIds = (1..20).map { "p1-$it" }),
            ),
        )
        server.enqueue(
            MockResponse().setBody(
                reviewsResponseJson(page = 2, totalPages = 5, totalResults = 100, reviewIds = (1..20).map { "p2-$it" }),
            ),
        )

        val snapshot = repository.getReviewsPagingData(movieId = 1).asSnapshot {
            scrollTo(index = 25)
        }

        assertEquals(40, snapshot.size)
        assertTrue(snapshot.map { it.id }.contains("p2-1"))
    }

    @Test
    fun `duplicate review ids across pages are deduplicated`() = runTest {
        val page1Ids = (1..19).map { "id$it" } + "dup"
        server.enqueue(
            MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 2, totalResults = 22, reviewIds = page1Ids)),
        )
        server.enqueue(
            MockResponse().setBody(reviewsResponseJson(page = 2, totalPages = 2, totalResults = 22, reviewIds = listOf("dup", "new"))),
        )

        val snapshot = repository.getReviewsPagingData(movieId = 1).asSnapshot {
            scrollTo(index = 19)
        }

        assertEquals(21, snapshot.size)
        assertEquals(1, snapshot.count { it.id == "dup" })
    }
}
