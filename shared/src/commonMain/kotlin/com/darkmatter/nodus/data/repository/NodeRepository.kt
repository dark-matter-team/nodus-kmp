package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.core.network.apiCall
import com.darkmatter.nodus.data.api.NodeApi
import com.darkmatter.nodus.data.model.ChecklistItemDto
import com.darkmatter.nodus.data.model.CompleteNodeResponse
import com.darkmatter.nodus.data.model.CreateNodeRequest
import com.darkmatter.nodus.data.model.NodeDto
import com.darkmatter.nodus.data.model.UpdateNodePositionRequest
import com.darkmatter.nodus.data.model.UpdateNodeRequest

/** Помилки бекенда (404 "Вузол не знайдено", 400 з причиною) повертаються як ApiResult.Error з його текстом. */
class NodeRepository(private val api: NodeApi) {

    suspend fun createNode(roadmapId: String, request: CreateNodeRequest): ApiResult<NodeDto> =
        apiCall { api.createNode(roadmapId, request) }

    suspend fun updateNode(nodeId: String, request: UpdateNodeRequest): ApiResult<NodeDto> =
        apiCall { api.updateNode(nodeId, request) }

    suspend fun updateNodePosition(nodeId: String, x: Double, y: Double): ApiResult<Unit> =
        apiCall { api.updateNodePosition(nodeId, UpdateNodePositionRequest(x, y)) }

    suspend fun deleteNode(nodeId: String): ApiResult<Unit> = apiCall { api.deleteNode(nodeId) }

    suspend fun toggleChecklistItem(itemId: String): ApiResult<ChecklistItemDto> =
        apiCall { api.toggleChecklistItem(itemId) }

    suspend fun completeNode(nodeId: String): ApiResult<CompleteNodeResponse> =
        apiCall { api.completeNode(nodeId) }
}
