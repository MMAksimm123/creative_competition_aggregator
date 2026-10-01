package com.example.creativecompetitionaggregator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.creativecompetitionaggregator.data.Reminder
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RemindersCalendarView(
    reminders: List<Reminder>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDelete: (Reminder) -> Unit,
    onOpen: (Reminder) -> Unit
) {
    var monthKey by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val currentMonth = remember(monthKey) { YearMonth.parse(monthKey) }

    val remindersByDate = remember(reminders) {
        reminders.groupBy {
            Instant.ofEpochMilli(it.triggerAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { monthKey = currentMonth.minusMonths(1).toString() }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
            }
            Text(
                text = currentMonth
                    .format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")))
                    .replaceFirstChar { it.titlecase(Locale("ru")) },
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { monthKey = currentMonth.plusMonths(1).toString() }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий месяц")
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        val firstOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value - 1 // 0 = Пн
        val daysInMonth = currentMonth.lengthOfMonth()
        val today = LocalDate.now()
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

        Column(Modifier.padding(horizontal = 8.dp)) {
            for (week in 0 until totalCells / 7) {
                Row(Modifier.fillMaxWidth()) {
                    for (day in 0 until 7) {
                        val cellIndex = week * 7 + day
                        val dayNumber = cellIndex - firstDayOfWeek + 1
                        if (dayNumber in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNumber)
                            DayCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                hasReminders = remindersByDate.containsKey(date),
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()

        val dayReminders = remindersByDate[selectedDate]
            .orEmpty()
            .sortedBy { it.triggerAt }

        if (dayReminders.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "На этот день напоминаний нет",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dayReminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onOpen = { onOpen(reminder) },
                        onDelete = { onDelete(reminder) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasReminders: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            if (hasReminders) {
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}