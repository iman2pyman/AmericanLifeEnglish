package com.ieamericanenglish.app

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import org.json.JSONArray
import java.util.Locale

data class VocabularyWord(
    val word: String,
    val meaningsFa: String,
    val partOfSpeech: String,
    val ipaUs: String,
    val cefr: String,
    val category: String,
    val definitionEn: String,
    val exampleEn: String,
    val exampleFa: String,
    val synonymsAntonyms: String,
    val wordFamily: String,
    val collocations: String
)

fun loadVocabulary(context: Context): List<VocabularyWord> {
    val json = context.assets.open("vocabulary_1000.json")
        .bufferedReader(Charsets.UTF_8).use { it.readText() }
    val array = JSONArray(json)
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        VocabularyWord(
            o.optString("word"), o.optString("meanings_fa"), o.optString("part_of_speech"),
            o.optString("ipa_us"), o.optString("cefr"), o.optString("category"),
            o.optString("definition_en"), o.optString("example_en"), o.optString("example_fa"),
            o.optString("synonyms_antonyms"), o.optString("word_family"), o.optString("collocations")
        )
    }
}

class AmericanSpeech(context: Context) {
    private var ready = false
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = engine?.setLanguage(Locale.US)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    fun speak(word: String) {
        if (ready) engine?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_" + word.hashCode())
    }

    fun close() {
        engine?.stop()
        engine?.shutdown()
        engine = null
    }
}
