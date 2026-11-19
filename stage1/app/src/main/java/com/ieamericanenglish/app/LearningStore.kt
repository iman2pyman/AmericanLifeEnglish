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
        val added = if (id in updated) { updated.remove(id); false } else { updated.add(id); true }
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
            "hard" -> if (oldInterval == 0L) 1L else (oldInterval * 1.2).toLong().coerceAtLeast(oldInterval + 1L)
            "good" -> if (oldInterval == 0L) 3L else (oldInterval * 2L).coerceAtLeast(3L)
            else -> if (oldInterval == 0L) 7L else (oldInterval * 2.5).toLong().coerceAtLeast(7L)
        }
        val due = now + if (rating == "again") 10L * 60L * 1000L else nextDays * DAY_MS
        schedule.put(id, JSONObject().put("dueAt", due).put("intervalDays", nextDays))
        prefs.edit().putString(KEY_SCHEDULE, schedule.toString()).apply()
        setWordStatus(word, if (nextDays >= 30L) "mastered" else "review")
        return due
    }

    /** Return the oldest due reviews first and cap the daily workload. */
    fun dueReviewWords(words: List<VocabularyWord>, level: String, limit: Int = DAILY_REVIEW_LIMIT): List<VocabularyWord> {
        val now = System.currentTimeMillis()
        val schedule = readJson(KEY_SCHEDULE)
        return words.asSequence()
            .filter { it.cefr.equals(level, true) }
            .distinctBy { wordId(it.word) }
            .mapNotNull { word ->
                val dueAt = schedule.optJSONObject(wordId(word.word))?.optLong("dueAt", Long.MAX_VALUE) ?: Long.MAX_VALUE
                if (dueAt <= now) dueAt to word else null
            }
            .sortedBy { it.first }
            .take(limit.coerceAtLeast(0))
            .map { it.second }
            .toList()
    }

    fun dueReviewCount(words: List<VocabularyWord>, level: String): Int {
        val now = System.currentTimeMillis()
        val schedule = readJson(KEY_SCHEDULE)
        return words.asSequence().filter { it.cefr.equals(level, true) }
            .distinctBy { wordId(it.word) }
            .count { (schedule.optJSONObject(wordId(it.word))?.optLong("dueAt", Long.MAX_VALUE) ?: Long.MAX_VALUE) <= now }
    }

    fun buildDailyPlan(words: List<VocabularyWord>, level: String, limit: Int = DAILY_NEW_WORD_GOAL): DailyLearningPlan {
        val today = todayKey()
        val progress = wordProgress()
        val eligible = words.asSequence().filter { it.cefr.equals(level, true) }
            .distinctBy { wordId(it.word) }.toList()
        val byId = eligible.associateBy { wordId(it.word) }
        val marks = bookmarks()
        val daily = readJson(KEY_DAILY)
        val dailyKey = "$today:$level"

        // Keep unfinished assignments from earlier days at the front of today's new-word queue.
        val priorAssignmentIds = mutableListOf<String>()
        val dailyKeys = daily.keys().asSequence()
            .filter { it.endsWith(":$level") && it != dailyKey }
            .filter { it.substringBefore(':') < today }
            .sorted().toList()
        dailyKeys.forEach { key ->
            val ids = daily.optJSONArray(key) ?: return@forEach
            for (i in 0 until ids.length()) priorAssignmentIds += ids.optString(i)
        }
        val unfinishedBacklog = priorAssignmentIds.distinct()
            .filter { (progress[it] ?: "new") == "learning" }
            .mapNotNull { byId[it] }
        // Also recover any in-progress words if an older app version did not persist a daily list.
        val unlistedLearning = eligible.filter { (progress[wordId(it.word)] ?: "new") == "learning" }
        val backlog = (unfinishedBacklog + unlistedLearning).distinctBy { wordId(it.word) }

        val freshMarked = eligible.filter { wordId(it.word) in marks && (progress[wordId(it.word)] ?: "new") == "new" }
        val freshOther = eligible.filter { wordId(it.word) !in marks && (progress[wordId(it.word)] ?: "new") == "new" }
        val storedToday = daily.optJSONArray(dailyKey)
        val today'sUnfinished = if (storedToday == null) emptyList() else
            (0 until storedToday.length()).mapNotNull { byId[storedToday.optString(it)] }
                .filter { (progress[wordId(it.word)] ?: "new") == "learning" }
        val selected = (today'sUnfinished + backlog + freshMarked + freshOther)
            .distinctBy { wordId(it.word) }.take(limit.coerceAtLeast(0))

        daily.put(dailyKey, JSONArray(selected.map { wordId(it.word) }))
        prefs.edit().putString(KEY_DAILY, daily.toString()).apply()
        selected.forEach { word ->
            if ((progress[wordId(word.word)] ?: "new") == "new") setWordStatus(word.word, "learning")
        }

        val scheduledIds = selected.map { wordId(it.word) }.toSet()
        val waiting = freshMarked.count { wordId(it.word) !in scheduledIds }
        val month = currentMonthPrefix()
        val completed = readJson(KEY_COMPLETED)
        val completedThisMonth = mutableSetOf<String>()
        val keys = completed.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key.startsWith(month)) {
                val ids = completed.optJSONArray(key) ?: continue
                for (index in 0 until ids.length()) completedThisMonth += ids.optString(index)
            }
        }
        val reviewTotal = dueReviewCount(eligible, level)
        val reviewToday = dueReviewWords(eligible, level)
        return DailyLearningPlan(
            today, selected, reviewToday, waiting, MONTHLY_NEW_WORD_GOAL,
            completedThisMonth.size, completedTodayCount(level),
            (reviewTotal - reviewToday.size).coerceAtLeast(0)
        )
    }

    /** Save the current daily session so the learner can resume after closing the app. */
    fun savePracticeSession(level: String, words: List<VocabularyWord>, index: Int) {
        val state = JSONObject()
            .put("level", level)
            .put("date", todayKey())
            .put("index", index.coerceAtLeast(0))
            .put("words", JSONArray(words.map { wordId(it.word) }))
        prefs.edit().putString(KEY_SESSION, state.toString()).apply()
    }

    /** Restore a same-day session, resolving saved word IDs against the current vocabulary bank. */
    fun restorePracticeSession(words: List<VocabularyWord>, level: String): Pair<List<VocabularyWord>, Int>? {
        val state = runCatching { JSONObject(prefs.getString(KEY_SESSION, "{}") ?: "{}") }.getOrNull() ?: return null
        if (state.optString("level") != level || state.optString("date") != todayKey()) return null
        val ids = state.optJSONArray("words") ?: return null
        val byId = words.distinctBy { wordId(it.word) }.associateBy { wordId(it.word) }
        val restored = (0 until ids.length()).mapNotNull { byId[ids.optString(it)] }
        if (restored.isEmpty()) return null
        return restored to state.optInt("index", 0).coerceIn(0, restored.size)
    }

    fun clearPracticeSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    fun markDailyWordCompleted(word: String, level: String? = null) {
        val today = todayKey()
        val completed = readJson(KEY_COMPLETED)
        val completionKey = if (level.isNullOrBlank()) today else "$today:$level"
        val ids = completed.optJSONArray(completionKey) ?: JSONArray()
        val id = wordId(word)
        if ((0 until ids.length()).none { ids.optString(it) == id }) ids.put(id)
        completed.put(completionKey, ids)
        prefs.edit().putString(KEY_COMPLETED, completed.toString()).apply()
    }

    fun completedTodayCount(level: String? = null): Int {
        val completed = readJson(KEY_COMPLETED)
        val key = if (level.isNullOrBlank()) todayKey() else "${todayKey()}:$level"
        return completed.optJSONArray(key)?.length()
            ?: if (level.isNullOrBlank()) 0 else completed.optJSONArray(todayKey())?.length() ?: 0
    }

    /** Persist every quiz response locally; keep a bounded history to protect storage. */
    fun recordQuizAnswer(word: String, questionType: String, selectedAnswer: String, correctAnswer: String, correct: Boolean) {
        val id = wordId(word)
        if (id.isBlank() || questionType.isBlank()) return
        val history = runCatching { JSONArray(prefs.getString(KEY_QUIZ_HISTORY, "[]") ?: "[]") }
            .getOrElse { JSONArray() }
        history.put(JSONObject()
            .put("word", id)
            .put("type", questionType)
            .put("selected", selectedAnswer)
            .put("expected", correctAnswer)
            .put("correct", correct)
            .put("timestamp", System.currentTimeMillis()))
        val bounded = JSONArray()
        val start = (history.length() - MAX_QUIZ_HISTORY).coerceAtLeast(0)
        for (index in start until history.length()) bounded.put(history.getJSONObject(index))
        prefs.edit().putString(KEY_QUIZ_HISTORY, bounded.toString()).apply()
    }

    fun quizStats(words: List<VocabularyWord>, level: String): QuizStats {
        val history = quizHistory()
        val levelIds = words.asSequence().filter { it.cefr.equals(level, true) }
            .map { wordId(it.word) }.toSet()
        var answered = 0
        var correct = 0
        val typeTotals = mutableMapOf<String, Pair<Int, Int>>()
        val wordTotals = mutableMapOf<String, Pair<Int, Int>>()
        for (index in 0 until history.length()) {
            val item = history.optJSONObject(index) ?: continue
            val word = item.optString("word")
            if (word !in levelIds) continue
            val type = item.optString("type")
            val isCorrect = item.optBoolean("correct", false)
            answered++
            if (isCorrect) correct++
            val typeOld = typeTotals[type] ?: (0 to 0)
            typeTotals[type] = (typeOld.first + 1) to (typeOld.second + if (isCorrect) 1 else 0)
            val wordOld = wordTotals[word] ?: (0 to 0)
            wordTotals[word] = (wordOld.first + 1) to (wordOld.second + if (isCorrect) 1 else 0)
        }
        val weak = wordTotals.entries.asSequence()
            .filter { it.key in levelIds && it.value.first >= 2 && it.value.second * 100 / it.value.first < 60 }
            .sortedBy { it.value.second.toDouble() / it.value.first }
            .map { it.key }.toList()
        val typeStats = typeTotals.map { (type, totals) ->
            QuizTypeStat(type, totals.first, totals.second)
        }.sortedBy { it.type }
        return QuizStats(answered, correct, typeStats, weak)
    }

    fun weakWords(words: List<VocabularyWord>, level: String): List<VocabularyWord> {
        val weakIds = quizStats(words, level).weakWordIds.toSet()
        return words.asSequence().filter { it.cefr.equals(level, true) }
            .distinctBy { wordId(it.word) }
            .filter { wordId(it.word) in weakIds }.toList()
    }

    private fun quizHistory(): JSONArray =
        runCatching { JSONArray(prefs.getString(KEY_QUIZ_HISTORY, "[]") ?: "[]") }
            .getOrElse { JSONArray() }

    private fun readJson(key: String): JSONObject =
        runCatching { JSONObject(prefs.getString(key, "{}") ?: "{}") }.getOrElse { JSONObject() }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun currentMonthPrefix(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    companion object {
        const val DAILY_NEW_WORD_GOAL = 15
        const val DAILY_REVIEW_LIMIT = 30
        const val MONTHLY_NEW_WORD_GOAL = 450
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
        private const val KEY_QUIZ_HISTORY = "quiz_answer_history"
        private const val KEY_SESSION = "daily_practice_session"
        private const val MAX_QUIZ_HISTORY = 5000
        fun wordId(word: String): String = word.trim().lowercase(Locale.US)
    }
}

data class DailyLearningPlan(
    val date: String,
    val newWords: List<VocabularyWord>,
    val reviewWords: List<VocabularyWord>,
    val bookmarkedWaiting: Int,
    val monthlyNewWordGoal: Int,
    val monthCompleted: Int,
    val completedToday: Int,
    val reviewBacklogCount: Int = 0
)


data class QuizTypeStat(val type: String, val answered: Int, val correct: Int) {
    val accuracyPercent: Int get() = if (answered == 0) 0 else correct * 100 / answered
}

data class QuizStats(
    val answered: Int,
    val correct: Int,
    val byType: List<QuizTypeStat>,
    val weakWordIds: List<String>
) {
    val accuracyPercent: Int get() = if (answered == 0) 0 else correct * 100 / answered
}
