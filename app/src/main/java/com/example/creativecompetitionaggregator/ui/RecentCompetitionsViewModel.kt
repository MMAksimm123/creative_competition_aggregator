package com.example.creativecompetitionaggregator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.creativecompetitionaggregator.data.CompetitionRepository
import com.example.creativecompetitionaggregator.data.CompetitionSummary
import com.example.creativecompetitionaggregator.data.Reminder
import com.example.creativecompetitionaggregator.data.ReminderDatabase
import com.example.creativecompetitionaggregator.data.ReminderRepository
import com.example.creativecompetitionaggregator.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID


data class RecentCompetitionsUiState(
    val isLoading: Boolean = false,
    val competitions: List<CompetitionSummary> = emptyList(),
    val error: String? = null
)

class RecentCompetitionsViewModel(application: Application) : AndroidViewModel(application) {

    private val competitionRepository = CompetitionRepository(application.applicationContext)
    private val reminderRepository: ReminderRepository by lazy {
        val dao = ReminderDatabase.get(application).reminderDao()
        val scheduler = ReminderScheduler(application.applicationContext)
        ReminderRepository(dao, scheduler)
    }

    private val _uiState = MutableStateFlow(RecentCompetitionsUiState())
    val uiState: StateFlow<RecentCompetitionsUiState> = _uiState.asStateFlow()

    // Список всех напоминаний — нужен, чтобы отображать бейдж на карточке
    val reminders: StateFlow<List<Reminder>> = reminderRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadCompetitions()
    }

    fun loadCompetitions(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                if (forceRefresh) competitionRepository.clearCache()
                val competitions = competitionRepository.getRecentCompetitions()
                _uiState.value = _uiState.value.copy(isLoading = false, competitions = competitions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    fun refresh() = loadCompetitions(forceRefresh = true)

    fun addReminder(competition: CompetitionSummary, description: String, triggerAt: Long) {
        viewModelScope.launch {
            val reminder = Reminder(
                id = UUID.randomUUID().toString(),
                competitionId = competition.id,
                competitionTitle = competition.title,
                sourceUrl = competition.sourceUrl,
                description = description.ifBlank { "Напоминание о конкурсе" },
                triggerAt = triggerAt
            )
            reminderRepository.add(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { reminderRepository.delete(reminder) }
    }
}