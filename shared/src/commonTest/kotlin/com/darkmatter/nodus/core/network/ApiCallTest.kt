package com.darkmatter.nodus.core.network

import com.darkmatter.nodus.core.config.ApiConfig
import com.darkmatter.nodus.core.config.getBaseUrl
import com.darkmatter.nodus.testutil.ApiTestEnvironment
import com.darkmatter.nodus.testutil.FakeResponse
import com.darkmatter.nodus.testutil.errorJson
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ApiCallTest {

    @Test
    fun timeoutHasOwnKindAndNoText() = runBlocking {
        val env = ApiTestEnvironment { throw HttpRequestTimeoutException("http://localhost/api/roadmaps", 15_000) }

        val error = assertIs<ApiResult.Error>(env.roadmapRepository.getMyRoadmaps())

        // Текст для тайм-ауту обирає UI з ресурсів, мережевий шар його не формує
        assertEquals(ApiResult.Error(ErrorKind.Timeout), error)
    }

    @Test
    fun connectionFailureIsNetworkKind() = runBlocking {
        val env = ApiTestEnvironment { throw IllegalStateException("connection refused") }

        val error = assertIs<ApiResult.Error>(env.roadmapRepository.getMyRoadmaps())

        assertEquals(ErrorKind.Network, error.kind)
    }

    @Test
    fun statusCodesMapToClientAndServerKinds() = runBlocking {
        val notFound = ApiTestEnvironment { FakeResponse(HttpStatusCode.NotFound, errorJson("Дерево не знайдено")) }
        val serverError = ApiTestEnvironment { FakeResponse(HttpStatusCode.InternalServerError, "boom") }

        val clientError = assertIs<ApiResult.Error>(notFound.roadmapRepository.getRoadmap("r1"))
        val crash = assertIs<ApiResult.Error>(serverError.roadmapRepository.getRoadmap("r1"))

        assertEquals(ApiResult.Error(ErrorKind.Client, 404, "Дерево не знайдено"), clientError)
        // Тіло не {"error"}: тексту немає, лише тип і код
        assertEquals(ApiResult.Error(ErrorKind.Server, 500), crash)
    }

    @Test
    fun onlyRealErrorRangesMapToClientAndServer() {
        assertEquals(ErrorKind.Client, ErrorKind.fromStatus(400))
        assertEquals(ErrorKind.Client, ErrorKind.fromStatus(499))
        assertEquals(ErrorKind.Server, ErrorKind.fromStatus(500))
        assertEquals(ErrorKind.Server, ErrorKind.fromStatus(599))
        assertEquals(ErrorKind.Unknown, ErrorKind.fromStatus(302))
        assertEquals(ErrorKind.Unknown, ErrorKind.fromStatus(600))
    }

    @Test
    fun configuredBaseUrlIsUsedForRequests() = runBlocking {
        try {
            ApiConfig.setBaseUrl("https://api.example.com/")
            val env = ApiTestEnvironment { FakeResponse(HttpStatusCode.OK, "[]") }

            env.roadmapRepository.getMyRoadmaps()

            val url = env.engine.requests.single().url
            assertEquals("https", url.protocol.name)
            assertEquals("api.example.com", url.host)
            assertEquals("/api/roadmaps", url.encodedPath)
        } finally {
            ApiConfig.setBaseUrl("")
        }
    }

    @Test
    fun blankBaseUrlFallsBackToLocalBackend() {
        ApiConfig.setBaseUrl("  ")

        assertEquals(getBaseUrl(), ApiConfig.baseUrl)
    }
}
