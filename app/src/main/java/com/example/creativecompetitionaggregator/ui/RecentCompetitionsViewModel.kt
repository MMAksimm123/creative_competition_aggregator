package com.example.creativecompetitionaggregator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.creativecompetitionaggregator.data.CompetitionRepository
import com.example.creativecompetitionaggregator.data.CompetitionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecentCompetitionsUiState(
    val isLoading: Boolean = false,
    val competitions: List<CompetitionSummary> = emptyList(),
    val error: String? = null
)

class RecentCompetitionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CompetitionRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(RecentCompetitionsUiState())
    val uiState: StateFlow<RecentCompetitionsUiState> = _uiState.asStateFlow()

    init {
        loadCompetitions()
    }

    fun loadCompetitions(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                if (forceRefresh) {
                    repository.clearCache()
                }
                val competitions = repository.getRecentCompetitions()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    competitions = competitions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    fun refresh() {
        loadCompetitions(forceRefresh = true)
    }
}