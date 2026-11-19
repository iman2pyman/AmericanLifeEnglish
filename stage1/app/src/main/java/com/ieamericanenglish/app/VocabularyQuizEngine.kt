package com.ieamericanenglish.app

import java.util.Locale

enum class QuizQuestionType(val titleFa: String) {
    MEANING_TO_FA("انتخاب معنی فارسی"),
    FA_TO_WORD("انتخاب واژه انگلیسی"),
    FILL_BLANK("جای خالی جمله"),
    SYNONYM("تشخیص مترادف"),
    USAGE("کاربرد درست در جمله")
}

data class VocabularyQuizQuestion(
    val type: QuizQuestionType,
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val targetWord: String,
    val explanation: String
)

object VocabularyQuizEngine {
    fun createQuiz(words: List<VocabularyWord>, level: String, count: Int = 10): List<VocabularyQuizQuestion> =
        createQuizFromPool(words.filter { it.cefr.equals(level, true) }, count)

    fun createQuizFromPool(words: List<VocabularyWord>, count: Int = 10): List<VocabularyQuizQuestion> {
        val requestedCount = count.coerceAtLeast(0)
        if (requestedCount == 0) return emptyList()
        val pool = words.asSequence()
            .filter { it.word.isNotBlank() && it.meaningsFa.isNotBlank() }
            .distinctBy { LearningStore.wordId(it.word) }
            .toList()
        if (pool.size < 4) return emptyList()

        val questions = mutableListOf<VocabularyQuizQuestion>()
        val targets = pool.shuffled()
        val usedTargets = mutableSetOf<String>()
        val types = QuizQuestionType.values().toList()

        // First try every question format once so a quiz can cover all available skills.
        for (type in types.shuffled()) {
            if (questions.size >= requestedCount) break
            for (target in targets) {
                val id = LearningStore.wordId(target.word)
                if (id in usedTargets) continue
                val question = buildQuestion(type, target, pool) ?: continue
                questions += question
                usedTargets += id
                break
            }
        }

        // Fill remaining places with valid questions; unsupported data formats are skipped.
        for (target in targets) {
            if (questions.size >= requestedCount) break
            val id = LearningStore.wordId(target.word)
            if (id in usedTargets) continue
            for (type in types.shuffled()) {
                val question = buildQuestion(type, target, pool) ?: continue
                questions += question
                usedTargets += id
                break
            }
        }
        return questions.shuffled()
    }

    private fun buildQuestion(
        type: QuizQuestionType,
        target: VocabularyWord,
        pool: List<VocabularyWord>
    ): VocabularyQuizQuestion? {
        val others = pool.filter { LearningStore.wordId(it.word) != LearningStore.wordId(target.word) }
        return when (type) {
            QuizQuestionType.MEANING_TO_FA -> {
                val answer = clean(target.meaningsFa) ?: return null
                val distractors = distinctDistractors(others.mapNotNull { clean(it.meaningsFa) }, answer)
                make(type, "معنی فارسی «${target.word}» کدام است؟", answer, distractors, target, target.meaningsFa)
            }
            QuizQuestionType.FA_TO_WORD -> {
                val answer = clean(target.word) ?: return null
                val distractors = distinctDistractors(others.mapNotNull { clean(it.word) }, answer)
                make(type, "کدام واژه با این معنی مطابقت دارد؟\n${target.meaningsFa}", answer, distractors, target, target.word)
            }
            QuizQuestionType.FILL_BLANK -> {
                val sentence = target.exampleEn.split("|").map { it.trim() }
                    .firstOrNull { it.contains(Regex("\\b" + Regex.escape(target.word) + "\\b", RegexOption.IGNORE_CASE)) }
                    ?: return null
                val blanked = sentence.replace(Regex("\\b" + Regex.escape(target.word) + "\\b", RegexOption.IGNORE_CASE), "______")
                val answer = clean(target.word) ?: return null
                make(type, "جای خالی را با واژه مناسب کامل کن:\n$blanked", answer,
                    distinctDistractors(others.mapNotNull { clean(it.word) }, answer), target, target.word)
            }
            QuizQuestionType.SYNONYM -> {
                val synonyms = extractSynonyms(target.synonymsAntonyms)
                val answer = synonyms.firstOrNull { it.isNotBlank() } ?: return null
                val distractors = distinctDistractors(others.mapNotNull { candidate ->
                    extractSynonyms(candidate.synonymsAntonyms).firstOrNull()
                }, answer)
                make(type, "کدام گزینه مترادف «${target.word}» است؟", answer, distractors, target,
                    "${target.word}: $answer")
            }
            QuizQuestionType.USAGE -> {
                val sentence = target.exampleEn.split("|").map { it.trim() }
                    .firstOrNull { it.contains(Regex("\\b" + Regex.escape(target.word) + "\\b", RegexOption.IGNORE_CASE)) }
                    ?: return null
                val answer = sentence
                val distractors = others.mapNotNull { other ->
                    val example = other.exampleEn.split("|").map { it.trim() }
                        .firstOrNull { it.contains(Regex("\\b" + Regex.escape(other.word) + "\\b", RegexOption.IGNORE_CASE)) }
                    example?.let { it to other.word }
                }.filter { it.first != answer }
                    .filter { !Regex("\\b" + Regex.escape(target.word) + "\\b", RegexOption.IGNORE_CASE).containsMatchIn(it.first) }
                    .distinctBy { it.first.lowercase(Locale.US) }
                    .shuffled().take(3).map { it.first }
                if (distractors.size < 3) return null
                VocabularyQuizQuestion(type, "کدام جمله نمونه کاربرد واژه «${target.word}» در بانک آموزشی است؟",
                    (distractors + answer).shuffled(), answer, target.word, sentence)
            }
        }
    }

    private fun make(
        type: QuizQuestionType, prompt: String, answer: String, distractors: List<String>,
        target: VocabularyWord, explanation: String
    ): VocabularyQuizQuestion? {
        if (distractors.size < 3) return null
        return VocabularyQuizQuestion(type, prompt, (distractors.take(3) + answer).shuffled(),
            answer, target.word, explanation)
    }

    private fun distinctDistractors(values: List<String>, answer: String): List<String> =
        values.asSequence().map { it.trim() }.filter { it.isNotBlank() }
            .filter { it.equals(answer, true).not() }
            .distinctBy { it.lowercase(Locale.US) }.shuffled().take(3).toList()

    private fun clean(value: String): String? = value.trim().takeIf { it.isNotBlank() }

    private fun extractSynonyms(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val synonymPart = Regex("(?i)(?:synonyms?|مترادف(?:ها)?|هم[ -]?معنی)\\s*[:：-]\\s*([^;|\\n]+)")
            .find(raw)?.groupValues?.getOrNull(1)
        val source = synonymPart ?: if (!raw.contains(Regex("(?i)antonyms?|متضاد"))) raw else ""
        return source.split(",", "،", ";", "|", "/")
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.isNotBlank() && it.length <= 50 }
    }
}
