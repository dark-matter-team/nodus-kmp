package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.model.ChecklistItemDto
import com.darkmatter.nodus.data.model.CompleteNodeResponse
import com.darkmatter.nodus.data.model.CreateNodeRequest
import com.darkmatter.nodus.data.model.NodeCategory
import com.darkmatter.nodus.data.model.UpdateNodeRequest
import com.darkmatter.nodus.testutil.ApiTestEnvironment
import com.darkmatter.nodus.testutil.FakeResponse
import com.darkmatter.nodus.testutil.badRequest
import com.darkmatter.nodus.testutil.bodyText
import com.darkmatter.nodus.testutil.errorJson
import com.darkmatter.nodus.testutil.nodeJson
import com.darkmatter.nodus.testutil.ok
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NodeRepositoryTest {

    @Test
    fun toggleReturnsNewItemState() = runBlocking {
        val env = ApiTestEnvironment {
            FakeResponse(ok, """{"id":"c1","title":"Install Python","isCompleted":true,"sortOrder":1}""")
        }

        val result = env.nodeRepository.toggleChecklistItem("c1")

        assertEquals(ApiResult.Success(ChecklistItemDto("c1", "Install Python", true, 1)), result)
        val request = env.engine.requests.single()
        assertEquals(HttpMethod.Patch, request.method)
        assertEquals("/api/checklist-items/c1/toggle", request.url.encodedPath)
    }

    @Test
    fun completeReturnsUnlockedNodes() = runBlocking {
        val env = ApiTestEnvironment {
            FakeResponse(ok, """{"completedNodeId":"n1","unlockedNodeIds":["n2","n3"],"isRoadmapCompleted":false}""")
        }

        val result = env.nodeRepository.completeNode("n1")

        assertEquals(ApiResult.Success(CompleteNodeResponse("n1", listOf("n2", "n3"), false)), result)
        val request = env.engine.requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/nodes/n1/complete", request.url.encodedPath)
    }

    @Test
    fun completeRejectionReturnsServerMessage() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(badRequest, errorJson("Вузол заблоковано")) }

        val result = env.nodeRepository.completeNode("n2")

        assertEquals(ApiResult.Error("Вузол заблоковано", 400), result)
    }

    @Test
    fun createSendsRootNodeWithNullParent() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(ok, nodeJson("n9")) }
        val request = CreateNodeRequest(
            parentNodeId = null,
            title = "New node",
            description = "Details",
            category = NodeCategory.Practice,
            estimatedMinutes = 30,
            positionX = 10.0,
            positionY = 20.0,
            checklist = listOf("Step 1"),
        )

        val result = env.nodeRepository.createNode("r1", request)

        assertIs<ApiResult.Success<*>>(result)
        val sent = env.engine.requests.single()
        assertEquals("/api/roadmaps/r1/nodes", sent.url.encodedPath)
        val body = Json.parseToJsonElement(sent.bodyText()).jsonObject
        // Бекенд чекає parentNodeId явно, тож null має бути в тілі
        assertEquals(JsonNull, body["parentNodeId"])
        assertEquals("Practice", body["category"]?.jsonPrimitive?.content)
        assertEquals("Step 1", body["checklist"]?.jsonArray?.single()?.jsonPrimitive?.content)
    }

    @Test
    fun updateUsesPut() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(ok, nodeJson("n1")) }
        val request = UpdateNodeRequest(null, "Title", "Description", NodeCategory.Theory, 60, emptyList())

        val result = env.nodeRepository.updateNode("n1", request)

        assertIs<ApiResult.Success<*>>(result)
        val sent = env.engine.requests.single()
        assertEquals(HttpMethod.Put, sent.method)
        assertEquals("/api/nodes/n1", sent.url.encodedPath)
    }

    @Test
    fun positionUpdateSendsCoordinates() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(ok, """{"id":"n1","positionX":12.5,"positionY":-3.0}""") }

        val result = env.nodeRepository.updateNodePosition("n1", 12.5, -3.0)

        assertEquals(ApiResult.Success(Unit), result)
        val sent = env.engine.requests.single()
        assertEquals("/api/nodes/n1/position", sent.url.encodedPath)
        val body = Json.parseToJsonElement(sent.bodyText()).jsonObject
        assertEquals(12.5, body["positionX"]?.jsonPrimitive?.content?.toDouble())
        assertEquals(-3.0, body["positionY"]?.jsonPrimitive?.content?.toDouble())
    }

    @Test
    fun deleteMissingNodeReturnsNotFound() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(HttpStatusCode.NotFound, errorJson("Вузол не знайдено")) }

        val result = env.nodeRepository.deleteNode("n404")

        assertEquals(ApiResult.Error("Вузол не знайдено", 404), result)
        assertEquals(HttpMethod.Delete, env.engine.requests.single().method)
    }
}
