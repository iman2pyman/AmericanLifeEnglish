package com.ieamericanenglish.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.Locale

/**
 * Small offline-first learning store.
 * SharedPreferences keeps this first foundation dependency-free; the public API
 * can later be moved behind Room without changing the screens.
 */
class LearningStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("american_life_learning", Context.MODE_PRIVATE)

    fun selectedLevel(): String = prefs.getString(KEY_LEVEL, "A1") ?: "A1"

    fun setSelectedLevel(level: String) {
        if (level in LEVELS) prefs.edit().putString(KEY_LEVEL, level).apply()
    }

    fun bookmarks(): Set<String> = readStringSet(KEY_BOOKMARKS)

    fun isBookmarked(word: String): Boolean = wordId(word) in bookmarks()

    fun toggleBookmark(word: String): Boolean {
        val id = wordId(word)
        if (id.isBlank()) return false
        val updated = bookmarks().toMutableSet()
        val added = if (id in updated) {
            updated.remove(id)
            false
        } else {
            updated.add(id)
            true
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, updated).apply()
        return added
    }

    fun wordProgress(): Map<String, String> {
        val json = runCatching { JSONObject(prefs.getString(KEY_PROGRESS, "{}") ?: "{}") }.getOrNull()
            ?: JSONObject()
        return json.keys().asSequence().associateWith { json.optString(it, "new") }
    }

    fun setWordStatus(word: String, status: String) {
        val allowed = setOf("new", "learning", "review", "mastered")
        if (status !in allowed) return
        val id = wordId(word)
        if (id.isBlank()) return
        val json = runCatching { JSONObject(prefs.getString(KEY_PROGRESS, "{}") ?: "{}") }.getOrNull()
            ?: JSONObject()
        json.put(id, status)
        prefs.edit().putString(KEY_PROGRESS, json.toString()).apply()
    }

    /**
     * Creates today's plan with bookmarked words first, then unseen words.
     * Reviews are tracked separately and never consume the new-word quota.
     * A word can only occur once in the plan.
     */
    fun buildDailyPlan(words: List<VocabularyWord>, level: String, limit: Int = DAILY_NEW_WORD_GOAL): DailyLearningPlan {
        val today = LocalDate.now().toString()
        val progress = wordProgress()
        val eligible = words.asSequence()
            .filter { it.cefr.equals(level, true) }
            .distinctBy { wordId(it.word) }
            .toList()
        val byId = eligible.associateBy { wordId(it.word) }
        val marked = bookmarks().mapNotNull { byId[it] }
        val ordinary = eligible.filter { wordId(it.word) !in bookmarks() }
        val newMarked = marked.filter { (progress[wordId(it.word)] ?: "new") == "new" }
        val newOrdinary = ordinary.filter { (progress[wordId(it.word)] ?: "new") == "new" }
        val selected = (newMarked + newOrdinary).distinctBy { wordId(it.word) }.take(limit.coerceAtLeast(0))
        val dueReviews = eligible.filter { progress[wordId(it.word)] == "review" || progress[wordId(it.word)] == "learning" }
        val dailyJson = runCatching { JSONObject(prefs.getString(KEY_DAILY, "{}") ?: "{}") }.getOrNull()
            ?: JSONObject()
        val stored = dailyJson.optJSONArray(today)
        val finalNew = if (stored != null) {
            (0 until stored.length()).mapNotNull { byId[stored.optString(it)] }
        } else {
            dailyJson.put(today, JSONArray(selected.map { wordId(it.word) }))
            prefs.edit().putString(KEY_DAILY, dailyJson.toString()).apply()
            selected
        }
        return DailyLearningPlan(
            date = today,
            newWords = finalNew,
            reviewWords = dueReviews.filterNot { candidate -> finalNew.any { wordId(it.word) == wordId(candidate.word) } },
            bookmarkedWaiting = (newMarked + newOrdinary).drop(finalNew.size).count { wordId(it.word) in bookmarks() },
            monthlyNewWordGoal = limit.coerceAtLeast(0) * 30
        )
    }

    private fun readStringSet(key: String): Set<String> =
        prefs.getStringSet(key, emptySet())?.toSet() ?: emptySet()

    companion object {
        const val DAILY_NEW_WORD_GOAL = 15
        val LEVELS = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        private const val KEY_LEVEL = "selected_level"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_PROGRESS = "word_progress"
        private const val KEY_DAILY = "daily_new_words"

        fun wordId(word: String): String = word.trim().lowercase(Locale.US)
    }
}

data class DailyLearningPlan(
    val date: String,
    val newWords: List<VocabularyWord>,
    val reviewWords: List<VocabularyWord>,
    val bookmarkedWaiting: Int,
    val monthlyNewWordGoal: Int
)
