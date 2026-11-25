package com.darkmatter.nodus.data.api

import com.darkmatter.nodus.data.model.GenerateRoadmapRequest
import com.darkmatter.nodus.data.model.KnowledgeLevel
import com.darkmatter.nodus.data.model.PublicRoadmapSummaryDto
import com.darkmatter.nodus.data.model.RoadmapFullDto
import com.darkmatter.nodus.data.model.RoadmapStatus
import com.darkmatter.nodus.data.model.RoadmapSummaryDto
import com.darkmatter.nodus.data.model.UpdateRoadmapStatusRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/** Дерева користувача (RoadmapsController, усі запити з Bearer) і публічний каталог. */
class RoadmapApi(private val client: HttpClient) {

    suspend fun getPublicRoadmaps(
        search: String? = null,
        tag: String? = null,
        level: KnowledgeLevel? = null,
    ): List<PublicRoadmapSummaryDto> {
        return client.get("/api/public-roadmaps") {
            parameter("search", search)
            parameter("tag", tag)
            parameter("level", level?.name)
        }.body()
    }

    /** Генерація через ШІ триває довше за звичайні 15 с, тому тайм-аут для неї окремий. */
    suspend fun generateRoadmap(request: GenerateRoadmapRequest): RoadmapFullDto =
        client.post("/api/roadmaps/generate") {
            setBody(request)
            timeout {
                requestTimeoutMillis = GENERATION_TIMEOUT_MS
                socketTimeoutMillis = GENERATION_TIMEOUT_MS
            }
        }.body()

    suspend fun getMyRoadmaps(): List<RoadmapSummaryDto> =
        client.get("/api/roadmaps").body()

    suspend fun getRoadmap(id: String): RoadmapFullDto =
        client.get("/api/roadmaps/$id").body()

    suspend fun updateRoadmapStatus(id: String, status: RoadmapStatus) {
        client.patch("/api/roadmaps/$id/status") { setBody(UpdateRoadmapStatusRequest(status)) }
    }

    suspend fun deleteRoadmap(id: String) {
        client.delete("/api/roadmaps/$id")
    }

    private companion object {
        const val GENERATION_TIMEOUT_MS = 120_000L
    }
}
