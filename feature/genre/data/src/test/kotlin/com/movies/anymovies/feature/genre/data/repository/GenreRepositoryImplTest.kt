package com.movies.anymovies.feature.genre.data.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.movies.anymovies.core.common.dispatchers.CoroutineDispatchers
import com.movies.anymovies.core.common.error.DomainError
import com.movies.anymovies.core.common.result.Result
import com.movies.anymovies.core.database.AppDatabase
import com.movies.anymovies.core.network.json.networkJson
import com.movies.anymovies.feature.genre.data.remote.GenreApi
import com.movies.anymovies.feature.genre.domain.model.Genre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
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
class GenreRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var database: AppDatabase
    private lateinit var api: GenreApi
    private lateinit var repository: GenreRepositoryImpl

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

        api = retrofit.create(GenreApi::class.java)

        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Unconfined)
            .build()

        repository = GenreRepositoryImpl(
            genreApi = api,
            genreDao = database.genreDao(),
            dispatchers = testDispatchers,
        )
    }

    @After
    fun tearDown() {
        database.close()
        server.shutdown()
    }

    @Test
    fun `observeGenres emits network result and persists cache on happy path`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"genres":[{"id":28,"name":"Action"},{"id":35,"name":"Comedy"}]}""",
            ),
        )

        val result = repository.observeGenres().first()

        assertEquals(
            Result.Success(listOf(Genre(28, "Action"), Genre(35, "Comedy"))),
            result,
        )
        assertEquals(2, database.genreDao().observeAll().first().size)
    }

    @Test
    fun `observeGenres emits success empty state for empty genres array`() = runTest {
        server.enqueue(MockResponse().setBody("""{"genres":[]}"""))

        val result = repository.observeGenres().first()

        assertEquals(Result.Success(emptyList<Genre>()), result)
    }

    @Test
    fun `observeGenres emits parse error when cache is empty and json is malformed`() = runTest {
        server.enqueue(MockResponse().setBody("""{"genres": not-json"""))

        val result = repository.observeGenres().first()

        assertEquals(Result.Error(DomainError.Parse), result)
    }

    @Test
    fun `observeGenres emits unauthorized error when cache is empty and api returns 401`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))

        val result = repository.observeGenres().first()

        assertEquals(Result.Error(DomainError.Unauthorized), result)
    }

    @Test
    fun `observeGenres emits server error when cache is empty and api returns 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.observeGenres().first()

        assertEquals(Result.Error(DomainError.Server), result)
    }

    @Test
    fun `observeGenres emits timeout error when cache is empty and request times out`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = repository.observeGenres().first()

        assertEquals(Result.Error(DomainError.Timeout), result)
    }

    @Test
    fun `observeGenres keeps stale cache intact and does not surface error when refresh fails`() = runTest {
        server.enqueue(MockResponse().setBody("""{"genres":[{"id":28,"name":"Action"}]}"""))
        repository.refreshGenres()

        server.enqueue(MockResponse().setResponseCode(500))
        val emissions = repository.observeGenres().take(2).toList()

        assertTrue(emissions.all { it is Result.Success })
        val lastGenres = (emissions.last() as Result.Success).data
        assertEquals(listOf(Genre(28, "Action")), lastGenres)
        assertEquals(1, database.genreDao().observeAll().first().size)
    }

    @Test
    fun `observeGenres replaces cache on successful background refresh`() = runTest {
        server.enqueue(MockResponse().setBody("""{"genres":[{"id":28,"name":"Action"}]}"""))
        repository.refreshGenres()

        server.enqueue(MockResponse().setBody("""{"genres":[{"id":35,"name":"Comedy"}]}"""))
        val emissions = repository.observeGenres().take(2).toList()

        val lastGenres = (emissions.last() as Result.Success).data
        assertEquals(listOf(Genre(35, "Comedy")), lastGenres)
    }

    @Test
    fun `observeGenres surfaces an error instead of crashing when the local database is unavailable`() = runTest {
        database.close()

        val result = repository.observeGenres().first()

        assertTrue(result is Result.Error)
    }
}
