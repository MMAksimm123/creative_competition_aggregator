package com.example.creativecompetitionaggregator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.creativecompetitionaggregator.data.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Напоминание о конкурсе"
        val description = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()

        NotificationHelper.showReminder(
            context = context,
            id = reminderId.hashCode(),
            title = title,
            description = description
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = ReminderDatabase.get(context).reminderDao()
                dao.deleteById(reminderId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}