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
    val pronunciationGuideFa: String,
    val cefr: String,
    val category: String,
    val definitionEn: String,
    val exampleEn: String,
    val exampleFa: String,
    val synonymsAntonyms: String,
    val wordFamily: String,
    val collocations: String
)

private fun JSONObject.readTextField(key: String): String {
    val value = opt(key) ?: return ""
    if (value === JSONObject.NULL) return ""
    return when (value) {
        is JSONObject, is JSONArray -> value.toString()
        else -> value.toString()
    }
}

/**
 * Reads a vocabulary asset defensively. A malformed or unexpected asset is
 * logged and skipped rather than crashing the entire application at launch.
 */
private fun readVocabularyArray(context: Context, assetName: String): JSONArray? {
    return try {
        val source = context.assets.open(assetName)
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val parsed = org.json.JSONTokener(source).nextValue()
        when (parsed) {
            is JSONArray -> parsed
            is JSONObject -> {
                // Some published assets contain one word record as the root object.
                // Keep that record instead of treating it as an invalid dataset.
                when {
                    parsed.has("word") -> JSONArray().put(parsed)
                    parsed.optJSONArray("words") != null -> parsed.optJSONArray("words")
                    parsed.optJSONArray("vocabulary") != null -> parsed.optJSONArray("vocabulary")
                    else -> {
                        android.util.Log.w("AmericanLifeEnglish", "$assetName is a JSON object without a word record or recognized wrapper")
                        null
                    }
                }
            }
            else -> {
                android.util.Log.w("AmericanLifeEnglish", "$assetName has an unsupported JSON root")
                null
            }
        }
    } catch (error: Exception) {
        android.util.Log.e("AmericanLifeEnglish", "Could not read vocabulary asset: $assetName", error)
        null
    }
}

fun loadVocabulary(context: Context): List<VocabularyWord> {
    val result = mutableListOf<VocabularyWord>()
    val assetNames = buildList {
        add("vocabulary_1000.json")
        addAll(
            context.assets.list("").orEmpty()
                .filter { it.matches(Regex("vocabulary_additional_\\d{3}\\.json")) }
                .sorted()
        )
    }.distinct()

    val levelOverrides = try {
        val source = context.assets.open("cefr_levels.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        JSONObject(source)
    } catch (error: Exception) {
        android.util.Log.e("AmericanLifeEnglish", "Could not read cefr_levels.json; using levels embedded in vocabulary records", error)
        JSONObject()
    }

    for (assetName in assetNames) {
        val array = readVocabularyArray(context, assetName) ?: continue
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index)
            if (item == null) {
                android.util.Log.w("AmericanLifeEnglish", "Skipping non-object record $index in $assetName")
                continue
            }

            val word = item.readTextField("word").trim()
            if (word.isEmpty()) {
                android.util.Log.w("AmericanLifeEnglish", "Skipping record without a word at index $index in $assetName")
                continue
            }

            val embeddedLevel = item.readTextField("cefr")
            val level = levelOverrides.optString(word.lowercase(Locale.US), embeddedLevel)
                .takeIf { it.isNotBlank() } ?: embeddedLevel

            result += VocabularyWord(
                word = word,
                meaningsFa = item.readTextField("meanings_fa"),
                partOfSpeech = item.readTextField("part_of_speech"),
                ipaUs = item.readTextField("ipa_us"),
                pronunciationGuideFa = item.readTextField("pronunciation_guide_fa"),
                cefr = level,
                category = item.readTextField("category"),
                definitionEn = item.readTextField("definition_en"),
                exampleEn = item.readTextField("example_en"),
                exampleFa = item.readTextField("example_fa"),
                synonymsAntonyms = item.readTextField("synonyms_antonyms"),
                wordFamily = item.readTextField("word_family"),
                collocations = item.readTextField("collocations")
            )
        }
    }

    android.util.Log.i("AmericanLifeEnglish", "Loaded ${result.size} vocabulary records from ${assetNames.size} assets")
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
