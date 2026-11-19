package com.ieamericanenglish.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Offline persistence for vocabulary learning, bookmarks and spaced repetition. */
class LearningStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("american_life_learning", Context.MODE_PRIVATE)

    fun selectedLevel(): String = prefs.getString(KEY_LEVEL, "A1") ?: "A1"
    fun setSelectedLevel(level: String) { if (level in LEVELS) prefs.edit().putString(KEY_LEVEL, level).apply() }
    fun bookmarks(): Set<String> = prefs.getStringSet(KEY_BOOKMARKS, emptySet())?.toSet() ?: emptySet()
    fun isBookmarked(word: String): Boolean = wordId(word) in bookmarks()

    fun toggleBookmark(word: String): Boolean {
        val id = wordId(word)
        if (id.isBlank()) return false
        val updated = bookmarks().toMutableSet()
        val added = if (id in updated) updated.add(id) else { updated.remove(id); false }
        prefs.edit().putStringSet(KEY_BOOKMARKS, updated).apply()
        return added
    }

    fun wordProgress(): Map<String, String> {
        val json = readJson(KEY_PROGRESS)
        return json.keys().asSequence().associateWith { json.optString(it, "new") }
    }

    fun setWordStatus(word: String, status: String) {
        if (status !in STATUSES) return
        val id = wordId(word)
        if (id.isBlank()) return
        readJson(KEY_PROGRESS).put(id, status).also { prefs.edit().putString(KEY_PROGRESS, it.toString()).apply() }
    }

    /** Store a review interval. Again=10m, Hard=1d, Good=3d, Easy=7d; later correct answers grow it. */
    fun recordAnswer(word: String, rating: String): Long {
        val id = wordId(word)
        if (id.isBlank() || rating !in RATINGS) return 0L
        val now = System.currentTimeMillis()
        val schedule = readJson(KEY_SCHEDULE)
        val oldInterval = schedule.optJSONObject(id)?.optLong("intervalDays", 0L) ?: 0L
        val nextDays = when (rating) {
            "again" -> 0L
            "hard" -> if (oldInterval == 0L) 1L else (oldInterval * 1.2).toLong().coerceAtLeast(1L)
            "good" -> if (oldInterval == 0L) 3L else (oldInterval * 2L).coerceAtLeast(3L)
            else -> if (oldInterval == 0L) 7L else (oldInterval * 2.5).toLong().coerceAtLeast(7L)
        }
        val due = now + if (rating == "again") 10L * 60L * 1000L else nextDays * DAY_MS
        schedule.put(id, JSONObject().put("dueAt", due).put("intervalDays", nextDays))
        prefs.edit().putString(KEY_SCHEDULE, schedule.toString()).apply()
        setWordStatus(word, if (nextDays >= 30L) "mastered" else "review")
        return due
    }

    fun dueReviewWords(words: List<VocabularyWord>, level: String): List<VocabularyWord> {
        val now = System.currentTimeMillis()
        val schedule = readJson(KEY_SCHEDULE)
        return words.asSequence().filter { it.cefr.equals(level, true) }
            .distinctBy { wordId(it.word) }
            .filter { (schedule.optJSONObject(wordId(it.word))?.optLong("dueAt") ?: Long.MAX_VALUE) <= now }
            .toList()
    }

    fun buildDailyPlan(words: List<VocabularyWord>, level: String, limit: Int = DAILY_NEW_WORD_GOAL): DailyLearningPlan {
        val today = todayKey()
        val progress = wordProgress()
        val eligible = words.asSequence().filter { it.cefr.equals(level, true) }
            .distinctBy { wordId(it.word) }.toList()
        val byId = eligible.associateBy { wordId(it.word) }
        val marks = bookmarks()
        val freshMarked = eligible.filter { wordId(it.word) in marks && (progress[wordId(it.word)] ?: "new") == "new" }
        val freshOther = eligible.filter { wordId(it.word) !in marks && (progress[wordId(it.word)] ?: "new") == "new" }
        val selected = (freshMarked + freshOther).distinctBy { wordId(it.word) }.take(limit.coerceAtLeast(0))
        val daily = readJson(KEY_DAILY)
        val stored = daily.optJSONArray(today)
        val todaysNew = if (stored != null) (0 until stored.length()).mapNotNull { byId[stored.optString(it)] }
        else {
            daily.put(today, JSONArray(selected.map { wordId(it.word) }))
            prefs.edit().putString(KEY_DAILY, daily.toString()).apply()
            selected.forEach { setWordStatus(it.word, "learning") }
            selected
        }
        val waiting = (freshMarked + freshOther).drop(todaysNew.size).count { wordId(it.word) in marks }
        val month = currentMonthPrefix()
        val completed = readJson(KEY_COMPLETED)
        var monthCompleted = 0
        val keys = completed.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key.startsWith(month)) monthCompleted += completed.optJSONArray(key)?.length() ?: 0
        }
        return DailyLearningPlan(today, todaysNew, dueReviewWords(eligible, level), waiting,
            limit.coerceAtLeast(0) * 30, monthCompleted)
    }

    fun markDailyWordCompleted(word: String) {
        val today = todayKey()
        val completed = readJson(KEY_COMPLETED)
        val ids = completed.optJSONArray(today) ?: JSONArray()
        val id = wordId(word)
        if ((0 until ids.length()).none { ids.optString(it) == id }) ids.put(id)
        completed.put(today, ids)
        prefs.edit().putString(KEY_COMPLETED, completed.toString()).apply()
    }

    fun completedTodayCount(): Int = readJson(KEY_COMPLETED).optJSONArray(todayKey())?.length() ?: 0

    private fun readJson(key: String): JSONObject =
        runCatching { JSONObject(prefs.getString(key, "{}") ?: "{}") }.getOrElse { JSONObject() }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun currentMonthPrefix(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    companion object {
        const val DAILY_NEW_WORD_GOAL = 15
        val LEVELS = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        private val STATUSES = setOf("new", "learning", "review", "mastered")
        private val RATINGS = setOf("again", "hard", "good", "easy")
        private const val DAY_MS = 24L * 60L * 60L * 1000L
        private const val KEY_LEVEL = "selected_level"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_PROGRESS = "word_progress"
        private const val KEY_DAILY = "daily_new_words"
        private const val KEY_SCHEDULE = "review_schedule"
        private const val KEY_COMPLETED = "completed_words_by_day"
        fun wordId(word: String): String = word.trim().lowercase(Locale.US)
    }
}

data class DailyLearningPlan(
    val date: String,
    val newWords: List<VocabularyWord>,
    val reviewWords: List<VocabularyWord>,
    val bookmarkedWaiting: Int,
    val monthlyNewWordGoal: Int,
    val monthCompleted: Int
)
