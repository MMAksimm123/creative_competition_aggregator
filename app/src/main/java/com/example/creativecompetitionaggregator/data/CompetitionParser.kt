package com.example.creativecompetitionaggregator.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class CompetitionParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val creativeKeywords = listOf(
        "изобразительн", "рисунок", "рисунка", "живопис", "творческ",
        "дизайн", "художник", "график", "скульптур", "арт-"
    )

    suspend fun parseVseKonkursy(): List<CompetitionSummary> = withContext(Dispatchers.IO) {
        val competitions = mutableListOf<CompetitionSummary>()

        try {
            val url = "https://vsekonkursy.ru/tvorcheskie-konkursy/konkursy-dizajna"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                .build()

            val html = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                response.body?.string() ?: return@withContext  emptyList()
            }

            val document = Jsoup.parse(html)

            val headers = document.select("h2")

            for (header in headers) {
                val linkElement = header.select("a") ?: continue
                val title = linkElement.text().trim()
                val href = linkElement.attr("href")

                if (title.isBlank() || href.isBlank()) continue

                val titleAndDesc = title + " " + header.nextElementSibling()?.text().orEmpty()
                if (!creativeKeywords.any { keywords ->
                    titleAndDesc.lowercase().contains(keywords)
                    }) continue

                val location = determineLocation(titleAndDesc)

                val fulUrl = if (href.startsWith("http")) href
                    else "https://vsekonkursy.ru/$href"

                competitions.add(
                    CompetitionSummary(
                        id = fulUrl.hashCode().toString(),
                        title = title,
                        location = location,
                        organizer = extractOrganizer(titleAndDesc),
                        sourceUrl = fulUrl
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        competitions
    }

    suspend fun parseRoskonkurs(): List<CompetitionSummary> = withContext(Dispatchers.IO) {
        val competitions = mutableListOf<CompetitionSummary>()

        try {
            val url = "https://roskonkurs.com/ru/student-contests/"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
                .build()

            val html = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                response.body?.string() ?: return@withContext emptyList()
            }

            val document = Jsoup.parse(html)
            val links = document.select("a")

            for (link in links) {
                val title = link.text().trim()
                val href = link.attr("href")

                if (title.isBlank() || href.isBlank()) continue
                if (!creativeKeywords.any { title.lowercase().contains(it) }) continue

                val fullUrl = if (href.startsWith("http")) href
                    else "https://roskonkurs.com$href"

                if (competitions.any { it.sourceUrl == fullUrl }) continue

                competitions.add(
                    CompetitionSummary(
                        id = fullUrl.hashCode().toString(),
                        title = title,
                        location = "Всероссийский",
                        organizer = "РОСконкурс",
                        sourceUrl = fullUrl
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        competitions
    }

    private fun determineLocation(text: String): String {
        val lower = text.lowercase()
        return  when {
            lower.contains("челябинск") || lower.contains("областной") -> "Челябинская область"
            lower.contains("всероссийск") || lower.contains("международн") -> "Всероссийский"
            lower.contains("городск") -> "Челябинск"
            else -> "Всероссийский"
        }
    }

    private fun extractOrganizer(text: String): String {
        val patterns = listOf(
            Regex("организатор[:\\s]+([^.]{5,80})", RegexOption.IGNORE_CASE),
            Regex("министерство\\s+[^.]{5,60}", RegexOption.IGNORE_CASE),
            Regex("фонд\\s+[^.]{5,60}", RegexOption.IGNORE_CASE),
            Regex("центр\\s+[^.]{5,60}", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val math = pattern.find(text)
            if (math != null) return math.value.trim()
        }

        return "Организатор не указан"
    }
}

