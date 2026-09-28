package com.example.creativecompetitionaggregator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey val id: String,
    val competitionId: String,
    val competitionTitle: String,
    val sourceUrl: String,
    val description: String,
    val triggerAt: Long,
    val createdAt: Long = System.currentTimeMillis()
)