package com.darkmatter.nodus.testutil

import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI

class FakeResponse(val status: HttpStatusCode, val body: String = "")

/**
 * Рушій Ktor без мережі: відповідь на кожен запит повертає [handler], усі запити зберігаються в [requests].
 * Робить те саме, що MockEngine з ktor-client-mock, але без додаткової залежності.
 * execute і callContext позначені в Ktor як InternalAPI, тому потрібен opt-in (так само в MockEngine).
 */
@OptIn(InternalAPI::class)
class FakeHttpEngine(
    private val handler: (HttpRequestData) -> FakeResponse,
) : HttpClientEngineBase("FakeHttpEngine") {

    val requests = mutableListOf<HttpRequestData>()

    override val config = HttpClientEngineConfig()

    // HttpTimeout вимагає, щоб рушій заявив підтримку таймаутів
    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> = setOf(HttpTimeoutCapability)

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        requests += data
        val response = handler(data)
        return HttpResponseData(
            statusCode = response.status,
            requestTime = GMTDate(),
            headers = headersOf(HttpHeaders.ContentType, "application/json; charset=utf-8"),
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(response.body),
            callContext = callContext(),
        )
    }
}

/** JSON-тіло запиту, яке ContentNegotiation передає рушію як TextContent. */
fun HttpRequestData.bodyText(): String = (body as? TextContent)?.text.orEmpty()
