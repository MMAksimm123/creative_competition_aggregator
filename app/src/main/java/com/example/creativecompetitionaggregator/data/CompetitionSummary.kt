package com.example.creativecompetitionaggregator.data

import kotlinx.serialization.Serializable

@Serializable
data class CompetitionSummary(
    val id: String,
    val title: String,
    val location: String,
    val organizer: String,
    val sourceUrl: String,
    val publishedAt: Long = 0L,
    val siteOrder: Int = 0
)
