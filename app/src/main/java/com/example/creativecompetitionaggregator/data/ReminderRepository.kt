package com.example.creativecompetitionaggregator.data

import com.example.creativecompetitionaggregator.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler
) {
    fun observeAll(): Flow<List<Reminder>> = dao.observeAll()

    suspend fun add(reminder: Reminder) {
        dao.insert(reminder)
        scheduler.schedule(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        dao.delete(reminder)
        scheduler.cancel(reminder)
    }

    suspend fun getAllOnce(): List<Reminder> = dao.getAllOnce()
}