package com.example.creativecompetitionaggregator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.creativecompetitionaggregator.data.Reminder
import com.example.creativecompetitionaggregator.data.ReminderDatabase
import com.example.creativecompetitionaggregator.data.ReminderRepository
import com.example.creativecompetitionaggregator.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReminderRepository by lazy {
        val dao = ReminderDatabase.get(application).reminderDao()
        val scheduler = ReminderScheduler(application.applicationContext)
        ReminderRepository(dao, scheduler)
    }

    val reminders: StateFlow<List<Reminder>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { repository.delete(reminder) }
    }
}