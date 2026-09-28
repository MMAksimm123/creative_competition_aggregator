package com.example.creativecompetitionaggregator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.creativecompetitionaggregator.data.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = ReminderDatabase.get(context).reminderDao()
                val scheduler = ReminderScheduler(context)
                val now = System.currentTimeMillis()

                dao.getAllOnce()
                    .filter { it.triggerAt > now }
                    .forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}