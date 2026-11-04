package com.darkmatter.nodus.data.api

import com.darkmatter.nodus.data.model.KnowledgeLevel
import com.darkmatter.nodus.data.model.PublicRoadmapSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

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
}