package com.darkmatter.nodus.ui.screens

import com.darkmatter.nodus.data.model.PublicRoadmapSummaryDto

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(val roadmaps: List<PublicRoadmapSummaryDto>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}