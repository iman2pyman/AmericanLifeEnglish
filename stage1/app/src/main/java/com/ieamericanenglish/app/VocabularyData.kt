package com.ieamericanenglish.app

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
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
    val arrays = mutableListOf<JSONArray>()
    arrays += JSONArray(context.assets.open("vocabulary_1000.json")
        .bufferedReader(Charsets.UTF_8).use { it.readText() })
    val additionalFiles = context.assets.list("") .orEmpty()
        .filter { it.matches(Regex("vocabulary_additional_\\d{3}\\.json")) }
        .sorted()
    for (name in additionalFiles) {
        arrays += JSONArray(context.assets.open(name)
            .bufferedReader(Charsets.UTF_8).use { it.readText() })
    }
    val levelJson = context.assets.open("cefr_levels.json")
        .bufferedReader(Charsets.UTF_8).use { it.readText() }
    val levelOverrides = JSONObject(levelJson)
    val result = mutableListOf<VocabularyWord>()
    for (array in arrays) {
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result += VocabularyWord(
                o.optString("word"), o.optString("meanings_fa"), o.optString("part_of_speech"),
                o.optString("ipa_us"), levelOverrides.optString(o.optString("word").lowercase(Locale.US), o.optString("cefr")), o.optString("category"),
                o.optString("definition_en"), o.optString("example_en"), o.optString("example_fa"),
                o.optString("synonyms_antonyms"), o.optString("word_family"), o.optString("collocations")
            )
        }
    }
    return result
}

class AmericanSpeech(context: Context) {
    private val appContext = context.applicationContext
    private var ready = false
    private var engine: TextToSpeech? = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = engine?.setLanguage(Locale.US)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    fun speak(word: String) {
        if (ready) {
            engine?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_" + word.hashCode())
        } else {
            Toast.makeText(appContext, "موتور گفتار انگلیسی یا صدای English (United States) روی گوشی آماده نیست.", Toast.LENGTH_SHORT).show()
        }
    }

    fun close() {
        engine?.stop()
        engine?.shutdown()
        engine = null
    }
}
