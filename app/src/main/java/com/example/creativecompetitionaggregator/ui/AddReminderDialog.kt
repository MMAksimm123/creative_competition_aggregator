package com.example.creativecompetitionaggregator.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.creativecompetitionaggregator.data.CompetitionSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddReminderDialog(
    competition: CompetitionSummary,
    onDismiss: () -> Unit,
    onSave: (description: String, triggerAt: Long) -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    val calendar = remember {
        Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
    }
    var selectedTimestamp by remember { mutableStateOf(calendar.timeInMillis) }

    val formattedDateTime = remember(selectedTimestamp) {
        val sdf = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
        sdf.format(Date(selectedTimestamp))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Напоминание о конкурсе") },
        text = {
            Column {
                Text(
                    text = competition.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Ваше описание") },
                    placeholder = { Text("Например: загрузить рисунок на сайт") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Когда напомнить:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = formattedDateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, day)
                            selectedTimestamp = calendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text("Выбрать дату")
                }

                TextButton(onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, 0)
                            selectedTimestamp = calendar.timeInMillis
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }) {
                    Text("Выбрать время")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(description, selectedTimestamp) },
                enabled = selectedTimestamp > System.currentTimeMillis()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}