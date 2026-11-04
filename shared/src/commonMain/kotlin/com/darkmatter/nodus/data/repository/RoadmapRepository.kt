package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.api.RoadmapApi
import com.darkmatter.nodus.data.model.KnowledgeLevel
import com.darkmatter.nodus.data.model.PublicRoadmapSummaryDto
import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException

class RoadmapRepository(private val api: RoadmapApi) {

    suspend fun getPublicRoadmaps(
        search: String? = null,
        tag: String? = null,
        level: KnowledgeLevel? = null,
    ): ApiResult<List<PublicRoadmapSummaryDto>> {
        return try {
            ApiResult.Success(api.getPublicRoadmaps(search, tag, level))
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            val code = e.response.status.value
            ApiResult.Error("Помилка сервера: $code", code)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Не вдалося підключитись до сервера")
        }
    }
}