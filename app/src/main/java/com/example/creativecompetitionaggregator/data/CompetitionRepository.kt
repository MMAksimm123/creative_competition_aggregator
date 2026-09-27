package com.example.creativecompetitionaggregator.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class CompetitionRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val parser = CompetitionParser()
    private val cacheFile: File get() = File(context.cacheDir, "competitions_cache.json")

    suspend fun getRecentCompetitions(): List<CompetitionSummary> = withContext(Dispatchers.IO) {
        val cached = readFromCache()
        if (cached.isNotEmpty()) return@withContext cached

        return@withContext refreshFromWeb()
    }

    suspend fun refreshFromWeb(): List<CompetitionSummary> = withContext(Dispatchers.IO) {
        val allCompetitions = mutableListOf<CompetitionSummary>()

        allCompetitions.addAll(parser.parseVseKonkursy())

        allCompetitions.addAll(parser.parseRoskonkurs())

        val unique = allCompetitions.distinctBy { it.sourceUrl }

        val sorted = sortByNewest(unique)

        if (unique.isNotEmpty()) {
            saveToCache(sorted)
        }

        sorted
    }

    private fun readFromCache(): List<CompetitionSummary> {
        return try {
            if (!cacheFile.exists()) return emptyList()
            val content = cacheFile.readText()
            if (content.isBlank()) return emptyList()
            val parsed = json.decodeFromString<List<CompetitionSummary>>(content)
            sortByNewest(parsed)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun saveToCache(competitions: List<CompetitionSummary>) {
        try {
            val content = json.encodeToString(competitions)
            cacheFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sortByNewest(list: List<CompetitionSummary>): List<CompetitionSummary> {
        return list.sortedWith(
            compareByDescending<CompetitionSummary> { it.publishedAt }
                .thenBy { it.siteOrder }
        )
    }

    fun clearCache() {
        if (cacheFile.exists()) cacheFile.delete()
    }
}