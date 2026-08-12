package com.movies.anymovies.feature.movies.data.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.core.database.movie.MovieRemoteKeyEntity
import com.movies.anymovies.core.network.json.networkJson
import com.movies.anymovies.feature.movies.data.remote.DiscoverMovieApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MovieRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var database: AppDatabase
    private lateinit var api: DiscoverMovieApi
    private lateinit var repository: MovieRepositoryImpl

    private val testDispatchers = object : CoroutineDispatchers {
        override val io = Dispatchers.Unconfined
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }

    @Before
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

        api = retrofit.create(DiscoverMovieApi::class.java)

        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Unconfined)
            .build()

        repository = MovieRepositoryImpl(
            discoverMovieApi = api,
            movieDao = database.movieDao(),
            dispatchers = testDispatchers,
        )
    }

    @After
    fun tearDown() {
        database.close()
        server.shutdown()
    }

    private fun discoverResponseJson(page: Int, totalPages: Int, totalResults: Int, movieIds: List<Int>): String {
        val results = movieIds.joinToString(",") { id ->
            """{"id":$id,"title":"Movie $id","poster_path":"/poster$id.jpg",""" +
                """"backdrop_path":"/backdrop$id.jpg","release_date":"2024-01-01",""" +
                """"vote_average":7.5,"vote_count":100,"overview":"Overview $id"}"""
        }
        return """{"page":$page,"results":[$results],"total_pages":$totalPages,"total_results":$totalResults}"""
    }

    @Test
    fun `observeMoviesByGenre emits empty success state when total_results is zero`() = runTest {
        server.enqueue(MockResponse().setBody(discoverResponseJson(page = 1, totalPages = 0, totalResults = 0, movieIds = emptyList())))

        val result = repository.observeMoviesByGenre(genreId = 28).first()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.items.isEmpty())
        assertEquals(0, data.totalResults)
    }

    @Test
    fun `observeMoviesByGenre emits error when initial page fails and no cache exists`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.observeMoviesByGenre(genreId = 28).first()

        assertEquals(Result.Error(DomainError.Server), result)
    }

    @Test
    fun `loadNextPage failure leaves already-loaded movies intact`() = runTest {
        server.enqueue(MockResponse().setBody(discoverResponseJson(page = 1, totalPages = 5, totalResults = 100, movieIds = listOf(1, 2))))
        repository.refresh(genreId = 28)

        server.enqueue(MockResponse().setResponseCode(500))
        val appendResult = repository.loadNextPage(genreId = 28)

        assertEquals(Result.Error(DomainError.Server), appendResult)
        val cachedMovies = database.movieDao().observeByGenre(genreId = 28).first()
        assertEquals(2, cachedMovies.size)
    }

    @Test
    fun `duplicate movie ids across pages are deduplicated`() = runTest {
        server.enqueue(MockResponse().setBody(discoverResponseJson(page = 1, totalPages = 5, totalResults = 100, movieIds = listOf(1, 2))))
        repository.refresh(genreId = 28)

        server.enqueue(MockResponse().setBody(discoverResponseJson(page = 2, totalPages = 5, totalResults = 100, movieIds = listOf(1, 3))))
        repository.loadNextPage(genreId = 28)

        val result = repository.observeMoviesByGenre(genreId = 28).first()
        val ids = (result as Result.Success).data.items.map { it.id }

        assertEquals(3, ids.size)
        assertEquals(listOf(1, 2, 3), ids.sorted())
    }

    @Test
    fun `loadNextPage terminates at the page 500 hard limit without issuing a request`() = runTest {
        database.movieDao().insertKeys(
            listOf(
                MovieRemoteKeyEntity(
                    movieId = 1,
                    genreId = 28,
                    page = 500,
                    isLastPage = false,
                    totalPages = 900,
                    totalResults = 18000,
                    fetchedAtMillis = System.currentTimeMillis(),
                ),
            ),
        )

        val result = repository.loadNextPage(genreId = 28)

        assertEquals(Result.Success(Unit), result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `concurrent loadNextPage triggers for the same genre issue only one request`() = runTest {
        server.enqueue(MockResponse().setBody(discoverResponseJson(page = 1, totalPages = 5, totalResults = 100, movieIds = listOf(1, 2))))
        repository.refresh(genreId = 28)

        server.enqueue(MockResponse().setBody(discoverResponseJson(page = 2, totalPages = 5, totalResults = 100, movieIds = listOf(3, 4))))
        server.enqueue(MockResponse().setBody(discoverResponseJson(page = 2, totalPages = 5, totalResults = 100, movieIds = listOf(3, 4))))

        val first = async { repository.loadNextPage(genreId = 28) }
        val second = async { repository.loadNextPage(genreId = 28) }
        awaitAll(first, second)

        assertEquals(2, server.requestCount)
    }
}
