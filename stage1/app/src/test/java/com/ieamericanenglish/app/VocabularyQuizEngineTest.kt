package com.ieamericanenglish.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyQuizEngineTest {
    private fun sampleWord(word: String, meaning: String, level: String = "A1") = VocabularyWord(
        word = word,
        meaningsFa = meaning,
        partOfSpeech = "noun",
        ipaUs = "/test/",
        cefr = level,
        category = "test",
        definitionEn = "A test definition for $word.",
        exampleEn = "I use $word every day.",
        exampleFa = "من هر روز از $word استفاده می‌کنم.",
        synonymsAntonyms = "Synonyms: ${word}like",
        wordFamily = "",
        collocations = ""
    )

    @Test
    fun quizHasFourDistinctOptionsAndOneQuestionPerTarget() {
        val words = listOf(
            sampleWord("apple", "سیب"), sampleWord("book", "کتاب"),
            sampleWord("chair", "صندلی"), sampleWord("door", "در"),
            sampleWord("earth", "زمین"), sampleWord("family", "خانواده"),
            sampleWord("garden", "باغ"), sampleWord("house", "خانه")
        )

        val questions = VocabularyQuizEngine.createQuiz(words, "A1", 10)

        assertTrue(questions.isNotEmpty())
        assertTrue(questions.size <= 8)
        assertEquals(questions.size, questions.map { LearningStore.wordId(it.targetWord) }.distinct().size)
        questions.forEach { question ->
            assertEquals(4, question.options.size)
            assertEquals(4, question.options.distinctBy { it.lowercase() }.size)
            assertTrue(question.correctAnswer in question.options)
        }
    }

    @Test
    fun quizDoesNotMixDifferentCefrLevels() {
        val words = listOf(
            sampleWord("apple", "سیب", "A1"), sampleWord("book", "کتاب", "A1"),
            sampleWord("chair", "صندلی", "A1"), sampleWord("door", "در", "A1"),
            sampleWord("earth", "زمین", "B1"), sampleWord("family", "خانواده", "B1"),
            sampleWord("garden", "باغ", "B1"), sampleWord("house", "خانه", "B1")
        )

        val questions = VocabularyQuizEngine.createQuiz(words, "A1", 10)

        assertEquals(4, questions.size)
        assertTrue(questions.all { question ->
            words.first { it.word == question.targetWord }.cefr == "A1" &&
                question.options.none { option -> words.any { it.cefr == "B1" && it.word == option } }
        })
    }

    @Test
    fun quizReturnsEmptyWhenFourPlausibleChoicesCannotBeBuilt() {
        val words = listOf(
            sampleWord("apple", "سیب"), sampleWord("book", "کتاب"),
            sampleWord("chair", "صندلی")
        )

        assertTrue(VocabularyQuizEngine.createQuiz(words, "A1", 10).isEmpty())
    }
}
