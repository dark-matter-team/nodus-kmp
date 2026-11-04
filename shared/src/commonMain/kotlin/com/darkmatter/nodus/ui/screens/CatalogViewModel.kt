package com.darkmatter.nodus.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkmatter.nodus.core.di.AppContainer
import com.darkmatter.nodus.core.network.ApiResult
import com.darkmatter.nodus.data.repository.RoadmapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val repository: RoadmapRepository = AppContainer.roadmapRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _uiState.value = CatalogUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getPublicRoadmaps()) {
                is ApiResult.Success -> CatalogUiState.Success(result.data)
                is ApiResult.Error -> CatalogUiState.Error(result.message)
            }
        }
    }
}