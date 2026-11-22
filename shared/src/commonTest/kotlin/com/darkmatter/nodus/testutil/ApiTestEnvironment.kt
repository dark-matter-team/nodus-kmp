package com.darkmatter.nodus.testutil

import com.darkmatter.nodus.core.network.createHttpClient
import com.darkmatter.nodus.core.session.SessionManager
import com.darkmatter.nodus.data.api.NodeApi
import com.darkmatter.nodus.data.api.RoadmapApi
import com.darkmatter.nodus.data.repository.NodeRepository
import com.darkmatter.nodus.data.repository.RoadmapRepository
import com.darkmatter.nodus.data.storage.TokenStorage
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.request.HttpRequestData

/** Репозиторії дерев і вузлів на фейковому рушії, з уже відкритою сесією (запити з Bearer). */
class ApiTestEnvironment(handler: (HttpRequestData) -> FakeResponse) {
    val tokenStorage = TokenStorage(InMemorySettings())
    val sessionManager = SessionManager(tokenStorage).apply { startSession(TEST_TOKEN) }
    val engine = FakeHttpEngine(handler)
    private val client = createHttpClient(
        sessionManager = sessionManager,
        engine = engine,
        logger = object : Logger {
            override fun log(message: String) = Unit
        },
    )
    val roadmapRepository = RoadmapRepository(RoadmapApi(client))
    val nodeRepository = NodeRepository(NodeApi(client))

    companion object {
        const val TEST_TOKEN = "test-token"
    }
}

/** Вузол у форматі бекенда; enum рядками, як віддає JsonStringEnumConverter. */
fun nodeJson(
    id: String,
    parentNodeId: String? = null,
    status: String = "Available",
    checklist: String = "[]",
) = """
    {"id":"$id","parentNodeId":${parentNodeId?.let { "\"$it\"" } ?: "null"},
     "title":"Node $id","description":"About $id","category":"Theory","estimatedMinutes":120,
     "status":"$status","positionX":0,"positionY":150.5,"checklist":$checklist}
""".trimIndent()

fun roadmapJson(id: String, nodes: String) = """
    {"id":"$id","title":"Python","status":"Active","isPublic":false,"tags":["Python","AI"],"nodes":[$nodes]}
""".trimIndent()
