package com.vantage.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Story(
    val id: String, val title: String, val section: String, val summary: String,
    val context: String, val source: String, val url: String, val readTime: String
)

data class Edition(
    val dateLabel: String = "LATEST EDITION",
    val kicker: String = "Understand what matters",
    val title: String = "A clearer view of what comes next.",
    val description: String = "A considered briefing on the ideas, people and technology shaping tomorrow.",
    val bigPicture: String = "VANTAGE connects individual developments to the wider forces behind them. The aim is not more noise, but better context.",
    val stories: List<Story> = emptyList()
)

class EditionRepository(context: Context) {
    private val prefs = context.getSharedPreferences("vantage_reader", Context.MODE_PRIVATE)
    private val cache = context.getSharedPreferences("vantage_cache", Context.MODE_PRIVATE)
    private val endpoint = "https://raw.githubusercontent.com/Jagadeeswarh/signal-ai-news/feature/vantage-android-foundation/content/editions/latest.json"

    fun savedIds(): Set<String> = prefs.getStringSet("saved_ids", emptySet())?.toSet() ?: emptySet()

    fun toggleSaved(id: String) {
        val next = savedIds().toMutableSet()
        if (!next.add(id)) next.remove(id)
        prefs.edit().putStringSet("saved_ids", next).apply()
    }

    fun loadCached(): Edition = runCatching {
        cache.getString("latest_json", null)?.let(::parseEdition)
    }.getOrNull() ?: Edition()

    suspend fun refresh(): Edition? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 10000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
            }
            try {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseEdition(body).also { cache.edit().putString("latest_json", body).apply() }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun parseEdition(raw: String): Edition {
        val json = JSONObject(raw)
        val items = json.optJSONArray("stories")
        val stories = buildList {
            if (items != null) for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                add(Story(
                    id = item.optString("id"), title = item.optString("title"),
                    section = item.optString("section", "Briefing"),
                    summary = item.optString("summary"), context = item.optString("context"),
                    source = item.optString("source", "VANTAGE"),
                    url = item.optString("url"), readTime = item.optString("readTime", "4 min read")
                ))
            }
        }
        return Edition(
            dateLabel = json.optString("dateLabel", "LATEST EDITION"),
            kicker = json.optString("kicker", "Understand what matters"),
            title = json.optString("title", "The daily briefing"),
            description = json.optString("description", ""),
            bigPicture = json.optString("bigPicture", ""),
            stories = stories
        )
    }
}
