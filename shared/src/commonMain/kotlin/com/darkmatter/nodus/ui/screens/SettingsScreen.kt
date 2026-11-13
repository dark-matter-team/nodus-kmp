package com.darkmatter.nodus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Налаштування", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { /* TODO: змінити тему */ }) {
                Text("Тема")
            }
            OutlinedButton(onClick = { /* TODO: вийти з акаунта */ }) {
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