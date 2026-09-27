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

            var orderIndex = 0

            for (header in headers) {
                val linkElement = header.select("a") ?: continue
                val title = linkElement.text().trim()
                val href = linkElement.attr("href")

                if (title.isBlank() || href.isBlank()) continue

                val titleAndDesc = title + " " + header.nextElementSibling()?.text().orEmpty()
                if (!creativeKeywords.any {
                    titleAndDesc.lowercase().contains(it)
                    }) continue

                val location = determineLocation(titleAndDesc)

                val contextText = header.parent()?.text().orEmpty()
                val publishedAt = parseDate(contextText)

                val fulUrl = if (href.startsWith("http")) href
                    else "https://vsekonkursy.ru/$href"

                competitions.add(
                    CompetitionSummary(
                        id = fulUrl.hashCode().toString(),
                        title = title,
                        location = location,
                        organizer = extractOrganizer(titleAndDesc),
                        sourceUrl = fulUrl,
                        publishedAt = publishedAt,
                        siteOrder = orderIndex++
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

            var orderIndex = 0

            for (link in links) {
                val title = link.text().trim()
                val href = link.attr("href")

                if (title.isBlank() || href.isBlank()) continue
                if (!creativeKeywords.any { title.lowercase().contains(it) }) continue

                val contextText = link.parent()?.text().orEmpty()
                val publishedAt = parseDate(contextText)

                val fullUrl = if (href.startsWith("http")) href
                    else "https://roskonkurs.com$href"

                if (competitions.any { it.sourceUrl == fullUrl }) continue

                competitions.add(
                    CompetitionSummary(
                        id = fullUrl.hashCode().toString(),
                        title = title,
                        location = "Всероссийский",
                        organizer = "РОСконкурс",
                        sourceUrl = fullUrl,
                        publishedAt = publishedAt,
                        siteOrder = orderIndex
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

    private fun parseDate(text: String): Long {
        val monthNames = mapOf(
            "январ" to 0, "феврал" to 1, "март" to 2, "апрел" to 3,
            "ма" to 4, "июн" to 5, "июл" to 6, "август" to 7,
            "сентябр" to 8, "октябр" to 9, "ноябр" to 10, "декабр" to 11
        )

        val ruPattern = Regex("""(\d{1,2})\s+([а-яё]+)\s+(\d{4})""", RegexOption.IGNORE_CASE)
        ruPattern.find(text)?.let { match ->
            val day = match.groupValues[1].toIntOrNull() ?: return@let
            val monthWord = match.groupValues[2].lowercase()
            val year = match.groupValues[3].toIntOrNull() ?: return@let
            val month = monthNames.entries.firstOrNull { monthWord.startsWith(it.key) }?.value
                ?: return@let
            return try {
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                cal.timeInMillis
            } catch (e: Exception) {
                0L
            }
        }

        val numericPattern = Regex("""(\d{1,2})[./](\d{1,2})[./](\d{4})""")
        numericPattern.find(text)?.let { match ->
            val day = match.groupValues[1].toIntOrNull() ?: return@let
            val month = (match.groupValues[2].toIntOrNull() ?: return@let) - 1
            val year = match.groupValues[3].toIntOrNull() ?: return@let
            return try {
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                cal.timeInMillis
            } catch (e: Exception) {
                0L
            }
        }

        val isoPattern = Regex("""(\d{4})-(\d{2})-(\d{2})""")
        isoPattern.find(text)?.let { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return@let
            val month = (match.groupValues[2].toIntOrNull() ?: return@let) - 1
            val day = match.groupValues[3].toIntOrNull() ?: return@let
            return try {
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                cal.timeInMillis
            } catch (e: Exception)
            {
                0L
            }
        }

        return 0L
    }
}

