package com.darkmatter.nodus.data.repository

import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.model.KnowledgeLevel
import com.darkmatter.nodus.data.model.NodeCategory
import com.darkmatter.nodus.data.model.NodeStatus
import com.darkmatter.nodus.data.model.RoadmapFullDto
import com.darkmatter.nodus.data.model.RoadmapStatus
import com.darkmatter.nodus.data.model.RoadmapSummaryDto
import com.darkmatter.nodus.testutil.ApiTestEnvironment
import com.darkmatter.nodus.testutil.FakeResponse
import com.darkmatter.nodus.testutil.bodyText
import com.darkmatter.nodus.testutil.errorJson
import com.darkmatter.nodus.testutil.nodeJson
import com.darkmatter.nodus.testutil.ok
import com.darkmatter.nodus.testutil.roadmapJson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RoadmapRepositoryTest {

    @Test
    fun myRoadmapsAreParsedWithEnumsAsStrings() = runBlocking {
        val env = ApiTestEnvironment {
            FakeResponse(
                ok,
                """[{"id":"r1","title":"Python","status":"Active","knowledgeLevel":"Beginner",
                    "timeCommitment":"1 hour a day","totalNodesCount":5,"completedNodesCount":2,"isPublic":false}]""",
            )
        }

        val result = env.roadmapRepository.getMyRoadmaps()

        val roadmap = assertIs<ApiResult.Success<List<RoadmapSummaryDto>>>(result).data.single()
        assertEquals(RoadmapStatus.Active, roadmap.status)
        assertEquals(KnowledgeLevel.Beginner, roadmap.knowledgeLevel)
        assertEquals(2, roadmap.completedNodesCount)
        val request = env.engine.requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/roadmaps", request.url.encodedPath)
        assertEquals("Bearer ${ApiTestEnvironment.TEST_TOKEN}", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun fullTreeIsParsedWithNodesAndChecklist() = runBlocking {
        val checklist = """[{"id":"c1","title":"Install Python","isCompleted":true,"sortOrder":1}]"""
        val env = ApiTestEnvironment {
            FakeResponse(ok, roadmapJson("r1", nodeJson("n1", checklist = checklist) + "," + nodeJson("n2", "n1", "Locked")))
        }

        val result = env.roadmapRepository.getRoadmap("r1")

        val tree = assertIs<ApiResult.Success<RoadmapFullDto>>(result).data
        assertEquals(listOf("Python", "AI"), tree.tags)
        val (root, child) = tree.nodes
        assertNull(root.parentNodeId)
        assertEquals(NodeCategory.Theory, root.category)
        assertEquals(150.5, root.positionY)
        assertEquals(true, root.checklist.single().isCompleted)
        assertEquals("n1", child.parentNodeId)
        assertEquals(NodeStatus.Locked, child.status)
        assertEquals("/api/roadmaps/r1", env.engine.requests.single().url.encodedPath)
    }

    @Test
    fun missingTreeReturnsServerMessage() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(HttpStatusCode.NotFound, errorJson("Дерево не знайдено")) }

        val result = env.roadmapRepository.getRoadmap("r404")

        assertEquals(ApiResult.Error("Дерево не знайдено", 404), result)
    }

    @Test
    fun generateSendsGoalLevelAndTime() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(ok, roadmapJson("r1", nodeJson("n1"))) }

        val result = env.roadmapRepository.generateRoadmap(" Learn Python ", KnowledgeLevel.Intermediate, "1 hour a day")

        assertIs<ApiResult.Success<*>>(result)
        val request = env.engine.requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/roadmaps/generate", request.url.encodedPath)
        val body = Json.parseToJsonElement(request.bodyText()).jsonObject
        assertEquals("Learn Python", body["goalPrompt"]?.jsonPrimitive?.content)
        assertEquals("Intermediate", body["knowledgeLevel"]?.jsonPrimitive?.content)
        assertEquals("1 hour a day", body["timeCommitment"]?.jsonPrimitive?.content)
    }

    @Test
    fun statusUpdateSendsEnumName() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(ok, """{"id":"r1","status":"Archived"}""") }

        val result = env.roadmapRepository.updateRoadmapStatus("r1", RoadmapStatus.Archived)

        assertEquals(ApiResult.Success(Unit), result)
        val request = env.engine.requests.single()
        assertEquals(HttpMethod.Patch, request.method)
        assertEquals("/api/roadmaps/r1/status", request.url.encodedPath)
        assertEquals("Archived", Json.parseToJsonElement(request.bodyText()).jsonObject["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun deleteAcceptsNoContent() = runBlocking {
        val env = ApiTestEnvironment { FakeResponse(HttpStatusCode.NoContent) }

        val result = env.roadmapRepository.deleteRoadmap("r1")

        assertEquals(ApiResult.Success(Unit), result)
        assertEquals(HttpMethod.Delete, env.engine.requests.single().method)
    }

    @Test
    fun networkFailureGivesConnectionMessage() = runBlocking {
        val env = ApiTestEnvironment { throw IllegalStateException("connection refused") }

        val result = env.roadmapRepository.getMyRoadmaps()

        assertEquals(ApiResult.Error("Не вдалося підключитись до сервера"), result)
    }
}
