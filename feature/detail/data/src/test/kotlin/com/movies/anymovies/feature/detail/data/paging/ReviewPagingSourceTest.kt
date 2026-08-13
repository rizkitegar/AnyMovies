package com.movies.anymovies.feature.detail.data.paging

import androidx.paging.PagingSource
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.network.json.networkJson
import com.movies.anymovies.feature.detail.data.remote.ReviewApi
import com.movies.anymovies.feature.detail.domain.model.ReviewLoadException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit

class ReviewPagingSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ReviewApi

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
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun pagingSource(movieId: Int = 1) =
        ReviewPagingSource(reviewApi = api, dispatchers = testDispatchers, movieId = movieId)

    private fun reviewsResponseJson(page: Int, totalPages: Int, totalResults: Int, reviewIds: List<String>): String {
        val results = reviewIds.joinToString(",") { id ->
            """{"id":"$id","author":"author-$id","author_details":{"name":"Author $id",""" +
                """"username":"user$id","avatar_path":null,"rating":7.0},"content":"Review $id",""" +
                """"created_at":"2026-01-01T00:00:00.000Z"}"""
        }
        return """{"page":$page,"results":[$results],"total_pages":$totalPages,"total_results":$totalResults}"""
    }

    @Test
    fun `load with a null key fetches page one`() = runTest {
        server.enqueue(
            MockResponse().setBody(reviewsResponseJson(page = 1, totalPages = 5, totalResults = 100, reviewIds = listOf("a", "b"))),
        )

        val result = pagingSource().load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(listOf("a", "b"), page.data.map { it.id })
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load appends the page requested by the key`() = runTest {
        server.enqueue(
            MockResponse().setBody(reviewsResponseJson(page = 3, totalPages = 5, totalResults = 100, reviewIds = listOf("c"))),
        )

        val result = pagingSource().load(PagingSource.LoadParams.Append(key = 3, loadSize = 20, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.prevKey)
        assertEquals(4, page.nextKey)
    }

    @Test
    fun `nextKey is null once total pages is reached`() = runTest {
        server.enqueue(
            MockResponse().setBody(reviewsResponseJson(page = 5, totalPages = 5, totalResults = 100, reviewIds = listOf("z"))),
        )

        val result = pagingSource().load(PagingSource.LoadParams.Append(key = 5, loadSize = 20, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey)
    }

    @Test
    fun `nextKey is capped at page 500 even when more total pages are reported`() = runTest {
        server.enqueue(
            MockResponse().setBody(reviewsResponseJson(page = 500, totalPages = 900, totalResults = 18000, reviewIds = listOf("a"))),
        )

        val result = pagingSource().load(PagingSource.LoadParams.Append(key = 500, loadSize = 20, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey)
    }

    @Test
    fun `a server error maps to a ReviewLoadException carrying the domain error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = pagingSource().load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        val error = result as PagingSource.LoadResult.Error
        val exception = error.throwable as ReviewLoadException
        assertEquals(DomainError.Server, exception.domainError)
    }
}
