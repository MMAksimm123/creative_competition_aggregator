package com.example.creativecompetitionaggregator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.creativecompetitionaggregator.RemindersViewModel
import java.time.LocalDate

enum class ViewMode { LIST, CALENDAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = viewModel()
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var mode by rememberSaveable { mutableStateOf(ViewMode.LIST) }
    var selectedEpochDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    val selectedDate = remember(selectedEpochDay) { LocalDate.ofEpochDay(selectedEpochDay) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Напоминания") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Переключатель режимов — теперь занимает всю ширину и корректно отображается
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = mode == ViewMode.LIST,
                    onClick = { mode = ViewMode.LIST },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Список")
                }
                SegmentedButton(
                    selected = mode == ViewMode.CALENDAR,
                    onClick = { mode = ViewMode.CALENDAR },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Календарь")
                }
            }

            when (mode) {
                ViewMode.LIST -> RemindersListView(
                    reminders = reminders,
                    onDelete = { viewModel.deleteReminder(it) },
                    onOpen = { openUrlInBrowser(context, it.sourceUrl) }
                )
                ViewMode.CALENDAR -> RemindersCalendarView(
                    reminders = reminders,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedEpochDay = it.toEpochDay() },
                    onDelete = { viewModel.deleteReminder(it) },
                    onOpen = { openUrlInBrowser(context, it.sourceUrl) }
                )
            }
        }
    }
}