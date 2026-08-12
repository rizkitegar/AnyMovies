package com.movies.anymovies.feature.detail.data.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.core.database.moviedetail.MovieDetailEntity
import com.movies.anymovies.core.network.json.networkJson
import com.movies.anymovies.feature.detail.data.remote.MovieDetailApi
import com.movies.anymovies.feature.detail.domain.model.MovieDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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
import kotlin.time.Duration.Companion.hours

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MovieDetailRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var database: AppDatabase
    private lateinit var api: MovieDetailApi
    private lateinit var repository: MovieDetailRepositoryImpl

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

        api = retrofit.create(MovieDetailApi::class.java)

        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Unconfined)
            .build()

        repository = MovieDetailRepositoryImpl(
            movieDetailApi = api,
            movieDetailDao = database.movieDetailDao(),
            dispatchers = testDispatchers,
        )
    }

    @After
    fun tearDown() {
        database.close()
        server.shutdown()
    }

    private fun detailJson(withAppendedVideos: Boolean, videosEmpty: Boolean = false): String {
        val videosBlock = when {
            !withAppendedVideos -> ""
            videosEmpty -> ""","videos":{"results":[]}"""
            else -> ""","videos":{"results":[{"id":"v1","key":"abc","name":"Official Trailer","site":"YouTube",""" +
                """"type":"Trailer","official":true,"published_at":"2024-01-01T00:00:00.000Z"}]}"""
        }
        return """{"id":1,"title":"Movie 1","tagline":"Tagline","overview":"Overview",""" +
            """"poster_path":"/poster.jpg","backdrop_path":"/backdrop.jpg","release_date":"2024-01-01",""" +
            """"runtime":120,"genres":[{"id":28,"name":"Action"}],"vote_average":7.5,"vote_count":100,""" +
            """"status":"Released","original_language":"en","homepage":"https://example.com"$videosBlock}"""
    }

    private fun fallbackVideosJson(): String =
        """{"id":1,"results":[{"id":"fallback1","key":"xyz","name":"Fallback Trailer","site":"YouTube",""" +
            """"type":"Trailer","official":false,"published_at":"2024-02-01T00:00:00.000Z"}]}"""

    @Test
    fun `observeMovieDetail emits not found error on 404 with no cache`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = repository.observeMovieDetail(movieId = 1).first()

        assertEquals(Result.Error(DomainError.NotFound), result)
    }

    @Test
    fun `observeMovieDetail caches and emits detail on success`() = runTest {
        server.enqueue(MockResponse().setBody(detailJson(withAppendedVideos = true)))

        val result = repository.observeMovieDetail(movieId = 1).first()

        assertTrue(result is Result.Success)
        val detail = (result as Result.Success).data
        assertEquals("Movie 1", detail.title)
        assertEquals(1, detail.videos.size)
        assertEquals("v1", detail.videos.first().id)
    }

    @Test
    fun `observeMovieDetail falls back to videos endpoint when appended block is absent`() = runTest {
        server.enqueue(MockResponse().setBody(detailJson(withAppendedVideos = false)))
        server.enqueue(MockResponse().setBody(fallbackVideosJson()))

        val result = repository.observeMovieDetail(movieId = 1).first()

        val detail = (result as Result.Success).data
        assertEquals(1, detail.videos.size)
        assertEquals("fallback1", detail.videos.first().id)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `observeMovieDetail falls back to videos endpoint when appended block is empty`() = runTest {
        server.enqueue(MockResponse().setBody(detailJson(withAppendedVideos = true, videosEmpty = true)))
        server.enqueue(MockResponse().setBody(fallbackVideosJson()))

        val result = repository.observeMovieDetail(movieId = 1).first()

        val detail = (result as Result.Success).data
        assertEquals(1, detail.videos.size)
        assertEquals("fallback1", detail.videos.first().id)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `observeMovieDetail does not call fallback when appended videos already present`() = runTest {
        server.enqueue(MockResponse().setBody(detailJson(withAppendedVideos = true)))

        repository.observeMovieDetail(movieId = 1).first()

        assertEquals(1, server.requestCount)
    }

    @Test
    fun `observeMovieDetail serves fresh cache without hitting network`() = runTest {
        database.movieDetailDao().insertDetail(cachedEntity(fetchedAtMillis = System.currentTimeMillis()))

        val result = repository.observeMovieDetail(movieId = 1).first()

        assertTrue(result is Result.Success)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `observeMovieDetail keeps serving stale cache when refresh fails`() = runTest {
        val staleFetchedAt = System.currentTimeMillis() - 7.hours.inWholeMilliseconds
        database.movieDetailDao().insertDetail(cachedEntity(fetchedAtMillis = staleFetchedAt))
        server.enqueue(MockResponse().setResponseCode(500))

        val results = repository.observeMovieDetail(movieId = 1).take(2).toList()

        assertTrue(results.all { it is Result.Success })
        assertEquals("Cached Title", (results.first() as Result.Success).data.title)
    }

    @Test
    fun `observeMovieDetail refreshes stale cache in background`() = runTest {
        val staleFetchedAt = System.currentTimeMillis() - 7.hours.inWholeMilliseconds
        database.movieDetailDao().insertDetail(cachedEntity(fetchedAtMillis = staleFetchedAt))
        server.enqueue(MockResponse().setBody(detailJson(withAppendedVideos = true)))

        val results = repository.observeMovieDetail(movieId = 1).take(2).toList()

        val titles = results.filterIsInstance<Result.Success<MovieDetail>>()
            .map { it.data.title }
        assertEquals(listOf("Cached Title", "Movie 1"), titles)
    }

    private fun cachedEntity(fetchedAtMillis: Long) = MovieDetailEntity(
        id = 1,
        title = "Cached Title",
        tagline = null,
        overview = "Cached overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2023-01-01",
        runtime = 100,
        genreIds = "28",
        genreNames = "Action",
        voteAverage = 6.0,
        voteCount = 50,
        status = "Released",
        originalLanguage = "en",
        homepage = null,
        fetchedAtMillis = fetchedAtMillis,
    )
}
