package com.example.creativecompetitionaggregator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.creativecompetitionaggregator.data.CompetitionSummary
import com.example.creativecompetitionaggregator.ui.theme.CreativeCompetitionAggregatorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentCompetitionsScreen(
    viewModel: RecentCompetitionsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Состояние для открытия диалога
    var reminderTarget by remember { mutableStateOf<CompetitionSummary?>(null) }

    // Запрос разрешения на уведомления (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* можно залогировать */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Недавние конкурсы") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ошибка: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) { Text("Повторить") }
                    }
                }
                uiState.competitions.isEmpty() -> {
                    Text("Конкурсы не найдены", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.competitions, key = { it.id }) { competition ->
                            val hasReminder = reminders.any { it.competitionId == competition.id }
                            CompetitionCard(
                                competition = competition,
                                hasReminder = hasReminder,
                                onOpenDetails = { url -> openUrlInBrowser(context, url) },
                                onAddReminder = { reminderTarget = competition }
                            )
                        }
                    }
                }
            }
        }
    }

    // Показываем диалог, если выбрана карточка
    reminderTarget?.let { competition ->
        AddReminderDialog(
            competition = competition,
            onDismiss = { reminderTarget = null },
            onSave = { description, triggerAt ->
                viewModel.addReminder(competition, description, triggerAt)
                reminderTarget = null
            }
        )
    }
}

@Composable
fun CompetitionCard(
    competition: CompetitionSummary,
    hasReminder: Boolean,
    onOpenDetails: (String) -> Unit,
    onAddReminder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = competition.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 ${competition.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🏢 ${competition.organizer}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            if (competition.publishedAt > 0L) {
                Spacer(modifier = Modifier.height(4.dp))
                val formatted = remember(competition.publishedAt) {
                    val sdf = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale("ru"))
                    sdf.format(java.util.Date(competition.publishedAt))
                }
                Text(
                    text = "📅 $formatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка напоминания
                TextButton(onClick = onAddReminder) {
                    Text(if (hasReminder) "🔔 Напоминание есть" else "🔔 Напомнить")
                }

                // Кнопка «Подробнее»
                TextButton(onClick = { onOpenDetails(competition.sourceUrl) }) {
                    Text("Подробнее →")
                }
            }
        }
    }
}

private fun openUrlInBrowser(context: android.content.Context, url: String) {
    try {
        val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context,android.net.Uri.parse(url))
    } catch (e: Exception) {
        try {
            context.startActivity(
                android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
            )
        } catch (ex: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(
                context,
                "Не удалось открыть ссылку.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentCompetitionsScreenPreview() {
    CreativeCompetitionAggregatorTheme {
        val fakeCompetition = CompetitionSummary(
            id = "1",
            title = "Всероссийский конкурс детского рисунка \"Моя Россия\"",
            location = "Всероссийский",
            organizer = "Министерство просвещения РФ",
            sourceUrl = ""
        )
        val reminders = false
        val hasReminder = reminders
        CompetitionCard(
            competition = fakeCompetition,
            onOpenDetails = {},
            onAddReminder = {},
            hasReminder = hasReminder
        )
    }
}