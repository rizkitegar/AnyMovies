package com.movies.anymovies.feature.detail.data.repository

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.network.json.networkJson
import com.movies.anymovies.feature.detail.data.remote.ReviewApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
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
import java.util.concurrent.TimeUnit

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
    fun `observeReviews emits empty success state when results array is empty`() = runTest {
        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 1, totalResults = 0, reviewIds = emptyList())))

        val result = repository.observeReviews(movieId = 1).first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.items.isEmpty())
        assertEquals(0, data.totalResults)
    }

    @Test
    fun `observeReviews emits error when initial page fails`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.observeReviews(movieId = 1).first()

        assertEquals(Result.Error(DomainError.Server), result)
    }

    @Test
    fun `loadNextPage failure leaves already-loaded reviews intact`() = runTest {
        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 5, totalResults = 100, reviewIds = listOf("a", "b"))))
        repository.observeReviews(movieId = 1).first()

        server.enqueue(MockResponse().setResponseCode(500))
        val appendResult = repository.loadNextPage(movieId = 1)

        assertEquals(Result.Error(DomainError.Server), appendResult)
        val current = repository.observeReviews(movieId = 1).first()
        assertEquals(2, (current as Result.Success).data.items.size)
    }

    @Test
    fun `duplicate review ids across pages are deduplicated`() = runTest {
        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 5, totalResults = 100, reviewIds = listOf("a", "b"))))
        repository.observeReviews(movieId = 1).first()

        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 2, totalPages = 5, totalResults = 100, reviewIds = listOf("a", "c"))))
        repository.loadNextPage(movieId = 1)

        val result = repository.observeReviews(movieId = 1).first()
        val ids = (result as Result.Success).data.items.map { it.id }

        assertEquals(3, ids.size)
        assertEquals(listOf("a", "b", "c"), ids.sorted())
    }

    @Test
    fun `loadNextPage terminates at the page 500 hard limit without issuing a request`() = runTest {
        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 900, totalResults = 18000, reviewIds = listOf("a"))))
        repository.observeReviews(movieId = 1).first()
        repeat(499) {
            server.enqueue(MockResponse().setBody(reviewsResponseJson(page = it + 2, totalPages = 900, totalResults = 18000, reviewIds = listOf("id$it"))))
        }
        repeat(499) {
            repository.loadNextPage(movieId = 1)
        }
        val requestsAtCap = server.requestCount

        val result = repository.loadNextPage(movieId = 1)

        assertEquals(Result.Success(Unit), result)
        assertEquals(requestsAtCap, server.requestCount)
    }

    @Test
    fun `concurrent loadNextPage triggers for the same movie issue only one request`() = runTest {
        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 5, totalResults = 100, reviewIds = listOf("a", "b"))))
        repository.observeReviews(movieId = 1).first()

        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 2, totalPages = 5, totalResults = 100, reviewIds = listOf("c", "d"))))
        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 2, totalPages = 5, totalResults = 100, reviewIds = listOf("c", "d"))))

        val first = async { repository.loadNextPage(movieId = 1) }
        val second = async { repository.loadNextPage(movieId = 1) }
        awaitAll(first, second)

        assertEquals(2, server.requestCount)
    }

    @Test
    fun `clear removes the in-memory session so the next observe re-fetches page one`() = runTest {
        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 5, totalResults = 100, reviewIds = listOf("a"))))
        repository.observeReviews(movieId = 1).first()

        repository.clear(movieId = 1)

        server.enqueue(MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 5, totalResults = 100, reviewIds = listOf("a"))))
        repository.observeReviews(movieId = 1).first()

        assertEquals(2, server.requestCount)
    }
}
