package com.movies.anymovies.feature.genre.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.movies.anymovies.core.network.json.networkJson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class GenreApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: GenreApi

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

        api = retrofit.create(GenreApi::class.java)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getGenres parses happy path response`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"genres":[{"id":28,"name":"Action"},{"id":35,"name":"Comedy"}]}""",
            ),
        )

        val response = api.getGenres()

        assertEquals(2, response.genres.size)
        assertEquals("Action", response.genres.first().name)
    }

    @Test
    fun `getGenres parses empty genres array`() = runTest {
        server.enqueue(MockResponse().setBody("""{"genres":[]}"""))

        val response = api.getGenres()

        assertEquals(emptyList<Any>(), response.genres)
    }

    @Test
    fun `getGenres throws SerializationException on malformed json`() {
        server.enqueue(MockResponse().setBody("""{"genres": not-json"""))

        assertThrows(SerializationException::class.java) {
            runBlocking { api.getGenres() }
        }
    }

    @Test
    fun `getGenres throws HttpException 401 on unauthorized`() {
        server.enqueue(MockResponse().setResponseCode(401))

        val exception = assertThrows(HttpException::class.java) {
            runBlocking { api.getGenres() }
        }
        assertEquals(401, exception.code())
    }

    @Test
    fun `getGenres throws HttpException 500 on server error`() {
        server.enqueue(MockResponse().setResponseCode(500))

        val exception = assertThrows(HttpException::class.java) {
            runBlocking { api.getGenres() }
        }
        assertEquals(500, exception.code())
    }

    @Test
    fun `getGenres throws SocketTimeoutException on timeout`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        assertThrows(SocketTimeoutException::class.java) {
            runBlocking { api.getGenres() }
        }
    }
}
