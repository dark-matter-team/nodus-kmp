package com.darkmatter.nodus.data.model

import kotlinx.serialization.Serializable

// Моделі RoadmapsController бекенда (DTOs/RoadmapDtos.cs).
// Enum бекенд серіалізує рядками (JsonStringEnumConverter): значення збігаються з іменами констант.

@Serializable
enum class RoadmapStatus {
    Active,
    Completed,
    Archived,
}

/** POST /api/roadmaps/generate. [timeCommitment] вільний текст, наприклад "1 година на день". */
@Serializable
data class GenerateRoadmapRequest(
    val goalPrompt: String,
    val knowledgeLevel: KnowledgeLevel,
    val timeCommitment: String,
)

@Serializable
data class UpdateRoadmapStatusRequest(val status: RoadmapStatus)

/** Елемент GET /api/roadmaps: дерева поточного користувача. */
@Serializable
data class RoadmapSummaryDto(
    val id: String,
    val title: String,
    val status: RoadmapStatus,
    val knowledgeLevel: KnowledgeLevel,
    val timeCommitment: String,
    val totalNodesCount: Int,
    val completedNodesCount: Int,
    val isPublic: Boolean,
)

/** Дерево з усіма вузлами: GET /api/roadmaps/{id} і відповідь генерації. */
@Serializable
data class RoadmapFullDto(
    val id: String,
    val title: String,
    val status: RoadmapStatus,
    val isPublic: Boolean,
    val tags: List<String> = emptyList(),
    val nodes: List<NodeDto> = emptyList(),
)
