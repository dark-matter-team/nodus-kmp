package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.core.network.apiCall
import com.darkmatter.nodus.data.api.RoadmapApi
import com.darkmatter.nodus.data.model.GenerateRoadmapRequest
import com.darkmatter.nodus.data.model.KnowledgeLevel
import com.darkmatter.nodus.data.model.PublicRoadmapSummaryDto
import com.darkmatter.nodus.data.model.RoadmapFullDto
import com.darkmatter.nodus.data.model.RoadmapStatus
import com.darkmatter.nodus.data.model.RoadmapSummaryDto

class RoadmapRepository(private val api: RoadmapApi) {

    suspend fun getPublicRoadmaps(
        search: String? = null,
        tag: String? = null,
        level: KnowledgeLevel? = null,
    ): ApiResult<List<PublicRoadmapSummaryDto>> = apiCall { api.getPublicRoadmaps(search, tag, level) }

    /** Генерує дерево під ціль і одразу зберігає його в акаунті користувача. */
    suspend fun generateRoadmap(
        goal: String,
        knowledgeLevel: KnowledgeLevel,
        timeCommitment: String,
    ): ApiResult<RoadmapFullDto> = apiCall {
        api.generateRoadmap(GenerateRoadmapRequest(goal.trim(), knowledgeLevel, timeCommitment.trim()))
    }

    suspend fun getMyRoadmaps(): ApiResult<List<RoadmapSummaryDto>> = apiCall { api.getMyRoadmaps() }

    /** 404 "Дерево не знайдено", якщо дерева немає або воно чуже. */
    suspend fun getRoadmap(id: String): ApiResult<RoadmapFullDto> = apiCall { api.getRoadmap(id) }

    suspend fun updateRoadmapStatus(id: String, status: RoadmapStatus): ApiResult<Unit> =
        apiCall { api.updateRoadmapStatus(id, status) }

    suspend fun deleteRoadmap(id: String): ApiResult<Unit> = apiCall { api.deleteRoadmap(id) }
}
