package com.darkmatter.nodus.data.api

import com.darkmatter.nodus.data.model.ChecklistItemDto
import com.darkmatter.nodus.data.model.CompleteNodeResponse
import com.darkmatter.nodus.data.model.CreateNodeRequest
import com.darkmatter.nodus.data.model.NodeDto
import com.darkmatter.nodus.data.model.UpdateNodePositionRequest
import com.darkmatter.nodus.data.model.UpdateNodeRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

/** Вузли дерева і пункти чеклиста (NodesController, усі запити з Bearer). */
class NodeApi(private val client: HttpClient) {

    suspend fun createNode(roadmapId: String, request: CreateNodeRequest): NodeDto =
        client.post("/api/roadmaps/$roadmapId/nodes") { setBody(request) }.body()

    suspend fun updateNode(nodeId: String, request: UpdateNodeRequest): NodeDto =
        client.put("/api/nodes/$nodeId") { setBody(request) }.body()

    suspend fun updateNodePosition(nodeId: String, request: UpdateNodePositionRequest) {
        client.patch("/api/nodes/$nodeId/position") { setBody(request) }
    }

    suspend fun deleteNode(nodeId: String) {
        client.delete("/api/nodes/$nodeId")
    }

    /** Перемикає пункт і повертає його новий стан. */
    suspend fun toggleChecklistItem(itemId: String): ChecklistItemDto =
        client.patch("/api/checklist-items/$itemId/toggle").body()

    suspend fun completeNode(nodeId: String): CompleteNodeResponse =
        client.post("/api/nodes/$nodeId/complete").body()
}
