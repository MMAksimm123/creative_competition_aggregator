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

        if (unique.isNotEmpty()) {
            saveToCache(unique)
        }

        unique
    }

    private fun readFromCache(): List<CompetitionSummary> {
        return try {
            if (!cacheFile.exists()) return emptyList()
            val content = cacheFile.readText()
            if (content.isBlank()) return emptyList()
            json.decodeFromString<List<CompetitionSummary>>(content)
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

    fun clearCache() {
        if (cacheFile.exists()) cacheFile.delete()
    }
    /*private val mockJsonData = """
         [
          {
            "id": "1",
            "title": "Всероссийский конкурс детского рисунка \"Моя Россия\"",
            "location": "Всероссийский",
            "organizer": "Министерство просвещения РФ",
            "sourceUrl": "https://example.com/1"
          },
          {
            "id": "2",
            "title": "Областной конкурс юных художников \"Уральская палитра\"",
            "location": "Челябинская область",
            "organizer": "Министерство образования Челябинской области",
            "sourceUrl": "https://example.com/2"
          },
          {
            "id": "3",
            "title": "Международный конкурс дизайна \"Арт-Пространство\"",
            "location": "Всероссийский",
            "organizer": "Фонд поддержки искусств",
            "sourceUrl": "https://example.com/3"
          }
        ]
    """.trimIndent()*/

//    suspend fun getRecentCompetitions(): List<CompetitionSummary> {
//        delay(1000)
//        return try {
//            json.decodeFromString<List<CompetitionSummary>>(mockJsonData)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }
}