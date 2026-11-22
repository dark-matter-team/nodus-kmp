package com.darkmatter.nodus.data.model

import kotlinx.serialization.Serializable

// Моделі NodesController бекенда (DTOs/NodeDtos.cs).

@Serializable
enum class NodeStatus {
    Locked,
    Available,
    Completed,
}

@Serializable
enum class NodeCategory {
    Theory,
    Practice,
    Project,
}

@Serializable
data class ChecklistItemDto(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val sortOrder: Int,
)

/** Вузол дерева; [parentNodeId] null у кореневого вузла. Позиція в координатах полотна. */
@Serializable
data class NodeDto(
    val id: String,
    val parentNodeId: String? = null,
    val title: String,
    val description: String,
    val category: NodeCategory,
    val estimatedMinutes: Int,
    val status: NodeStatus,
    val positionX: Double,
    val positionY: Double,
    val checklist: List<ChecklistItemDto> = emptyList(),
)

/** POST /api/roadmaps/{roadmapId}/nodes. [checklist] містить лише назви нових пунктів. */
@Serializable
data class CreateNodeRequest(
    val parentNodeId: String?,
    val title: String,
    val description: String,
    val category: NodeCategory,
    val estimatedMinutes: Int,
    val positionX: Double,
    val positionY: Double,
    val checklist: List<String>,
)

/** PUT /api/nodes/{nodeId}: повна заміна полів і чеклиста вузла. */
@Serializable
data class UpdateNodeRequest(
    val parentNodeId: String?,
    val title: String,
    val description: String,
    val category: NodeCategory,
    val estimatedMinutes: Int,
    val checklist: List<ChecklistItemDto>,
)

@Serializable
data class UpdateNodePositionRequest(
    val positionX: Double,
    val positionY: Double,
)

/** POST /api/nodes/{nodeId}/complete: які вузли розблокувались і чи завершено все дерево. */
@Serializable
data class CompleteNodeResponse(
    val completedNodeId: String,
    val unlockedNodeIds: List<String> = emptyList(),
    val isRoadmapCompleted: Boolean,
)
