package com.darkmatter.nodus.data.model

import kotlinx.serialization.Serializable

/** Елемент відповіді GET /api/public-roadmaps. */
@Serializable
data class PublicRoadmapSummaryDto(
    val id: String,
    val title: String? = null,
    val publicDescription: String? = null,
    val authorNickname: String? = null,
    val authorAvatarUrl: String? = null,
    val knowledgeLevel: KnowledgeLevel,
    val tags: List<String>? = null,
    val nodesCount: Int = 0,
    val clonesCount: Int = 0,
)
