package com.darkmatter.nodus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CatalogScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeContentPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Каталог", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { /* TODO: завантажити публічні дорожні карти */ }) {
                Text("Оновити")
            }
        }
    }
}
