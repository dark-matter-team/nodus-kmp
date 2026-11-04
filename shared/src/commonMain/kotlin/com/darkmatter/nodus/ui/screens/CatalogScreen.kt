package com.darkmatter.nodus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.darkmatter.nodus.data.model.PublicRoadmapSummaryDto

@Composable
fun CatalogScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = viewModel { CatalogViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Каталог", style = MaterialTheme.typography.headlineMedium)
                OutlinedButton(onClick = onOpenSettings) {
                    Text("Налаштування")
                }
            }
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when (val s = state) {
                    is CatalogUiState.Loading -> CircularProgressIndicator()

                    is CatalogUiState.Error -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(s.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::retry) {
                            Text("Повторити")
                        }
                    }

                    is CatalogUiState.Success -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(s.roadmaps, key = { it.id }) { roadmap ->
                            RoadmapCard(roadmap)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapCard(roadmap: PublicRoadmapSummaryDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = roadmap.title ?: "Без назви",
                style = MaterialTheme.typography.titleMedium,
            )
            roadmap.publicDescription?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = "Автор: ${roadmap.authorNickname ?: "невідомо"}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Рівень: ${roadmap.knowledgeLevel.name} • Вузлів: ${roadmap.nodesCount} • Клонувань: ${roadmap.clonesCount}",
                style = MaterialTheme.typography.bodySmall,
            )
            roadmap.tags?.takeIf { it.isNotEmpty() }?.let { tags ->
                Text(
                    text = tags.joinToString(" • "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}