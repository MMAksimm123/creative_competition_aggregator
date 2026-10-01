package com.example.creativecompetitionaggregator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.creativecompetitionaggregator.data.Reminder

@Composable
fun RemindersListView(
    reminders: List<Reminder>,
    onDelete: (Reminder) -> Unit,
    onOpen: (Reminder) -> Unit
) {
    if (reminders.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Пока нет напоминаний",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val sorted = reminders.sortedBy { it.triggerAt }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sorted, key = { it.id }) { reminder ->
            ReminderCard(
                reminder = reminder,
                onOpen = { onOpen(reminder) },
                onDelete = { onDelete(reminder) }
            )
        }
    }
}