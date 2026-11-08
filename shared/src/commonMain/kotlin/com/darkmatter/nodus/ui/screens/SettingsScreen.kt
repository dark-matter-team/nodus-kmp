package com.darkmatter.nodus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Налаштування", style = MaterialTheme.typography.headlineMedium)
            when (val s = state) {
                is SettingsUiState.Loading -> CircularProgressIndicator()
                is SettingsUiState.Success -> Text(
                    text = "Ви увійшли як ${s.user.nickname} (${s.user.email})",
                    style = MaterialTheme.typography.bodyMedium,
                )
                is SettingsUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::retry) {
                        Text("Повторити")
                    }
                }
            }
            Button(onClick = { /* TODO: змінити тему */ }) {
                Text("Тема")
            }
            OutlinedButton(onClick = viewModel::logout) {
                Text("Вийти")
            }
            OutlinedButton(onClick = { /* TODO: видалити акаунт */ }) {
                Text("Видалити акаунт")
            }
            // На Desktop немає системної кнопки "Назад", тож вона потрібна в інтерфейсі
            OutlinedButton(onClick = onBack) {
                Text("Назад")
            }
        }
    }
}
