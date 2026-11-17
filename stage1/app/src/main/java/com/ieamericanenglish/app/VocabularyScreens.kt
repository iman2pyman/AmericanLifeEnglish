package com.ieamericanenglish.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LevelColors = mapOf(
    "A1" to Color(0xFF0F9D91), "A2" to Color(0xFF2563EB),
    "B1" to Color(0xFF8B5CF6), "B2" to Color(0xFFB7791F),
    "C1" to Color(0xFFDB6B35), "C2" to Color(0xFFBE4778)
)

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack) { Text("→ بازگشت", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color(0xFF14213D), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SearchScreen(words: List<VocabularyWord>, onSpeak: (String) -> Unit, onBack: () -> Unit, onWord: (VocabularyWord) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, words) {
        if (query.isBlank()) emptyList()
        else words.filter { it.word.contains(query.trim(), true) || it.meaningsFa.contains(query.trim(), true) }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC)).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("جستجوی واژگان", onBack)
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, label = { Text("کلمه انگلیسی یا معنی فارسی") },
            placeholder = { Text("مثلاً: work یا کار") }, shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), focusedLabelColor = Color(0xFF2563EB))
        )
        Spacer(Modifier.height(10.dp))
        Text(if (query.isBlank()) "جستجو در ۱۰۰۰ واژه" else results.size.toString() + " نتیجه", color = Color(0xFF718096), fontSize = 12.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
            items(results, key = { it.word }) { word -> WordRow(word, onSpeak) { onWord(word) } }
        }
    }
}

@Composable
fun LearningScreen(words: List<VocabularyWord>, onSpeak: (String) -> Unit, onBack: () -> Unit, onWord: (VocabularyWord) -> Unit) {
    var level by remember { mutableStateOf("A1") }
    val levelWords = remember(level, words) { words.filter { it.cefr.equals(level, true) } }
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC)).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("آموزش واژگان", onBack)
        Text("سطح یادگیری را انتخاب کن", color = Color(0xFF18243A), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { item ->
                val tint = LevelColors[item] ?: Color(0xFF2563EB)
                FilterChip(
                    selected = level == item, onClick = { level = item },
                    label = { Text(item, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = tint, selectedLabelColor = Color.White)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(LevelColors[level] ?: Color(0xFF2563EB)))
            Spacer(Modifier.width(7.dp))
            Text("سطح " + level + "  •  " + levelWords.size + " واژه", color = Color(0xFF718096), fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
            items(levelWords, key = { it.word }) { word -> WordRow(word, onSpeak) { onWord(word) } }
        }
    }
}

@Composable
fun WordRow(word: VocabularyWord, onSpeak: (String) -> Unit, onClick: () -> Unit) {
    val accent = LevelColors[word.cefr] ?: Color(0xFF2563EB)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(horizontal = 13.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(word.word, color = accent, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    Text(word.ipaUs, color = Color(0xFF718096), fontSize = 11.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(word.meaningsFa, color = Color(0xFF18243A), fontSize = 12.sp, maxLines = 2)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(word.cefr, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onSpeak(word.word) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) {
                    Text("🔊", fontSize = 17.sp)
                }
            }
        }
    }
}

@Composable
fun WordDetailScreen(word: VocabularyWord, onSpeak: (String) -> Unit, onBack: () -> Unit) {
    val accent = LevelColors[word.cefr] ?: Color(0xFF2563EB)
    val englishExamples = word.exampleEn.split("|").map { it.trim() }.filter { it.isNotBlank() }
    val persianExamples = word.exampleFa.split("|").map { it.trim() }.filter { it.isNotBlank() }
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC)).verticalScroll(rememberScrollState()).padding(18.dp)) {
        ScreenHeader("اطلاعات واژه", onBack)
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 3.dp) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(word.word, color = accent, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                        Text(word.ipaUs, color = Color(0xFF718096), fontSize = 15.sp)
                    }
                    TextButton(onClick = { onSpeak(word.word) }) { Text("🔊 تلفظ", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    InfoTag(word.cefr, accent)
                    InfoTag(word.partOfSpeech, Color(0xFF2563EB))
                }
                Spacer(Modifier.height(16.dp))
                DetailLabel("معنی فارسی")
                Text(word.meaningsFa, color = Color(0xFF18243A), fontSize = 15.sp, lineHeight = 27.sp)
                Spacer(Modifier.height(14.dp))
                DetailLabel("تعریف انگلیسی")
                Text(word.definitionEn, color = Color(0xFF18243A), fontSize = 14.sp, lineHeight = 23.sp)
                Spacer(Modifier.height(14.dp))
                DetailLabel("مثال‌های کاربردی")
                englishExamples.forEachIndexed { index, example ->
                    Text("• " + example, color = Color(0xFF2563EB), fontSize = 13.sp, lineHeight = 21.sp)
                    if (index < persianExamples.size) Text(persianExamples[index], color = Color(0xFF718096), fontSize = 12.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(7.dp))
                }
                if (word.wordFamily.isNotBlank()) {
                    Spacer(Modifier.height(8.dp)); DetailLabel("خانواده واژگانی")
                    Text(word.wordFamily, color = Color(0xFF18243A), fontSize = 13.sp, lineHeight = 21.sp)
                }
                if (word.collocations.isNotBlank()) {
                    Spacer(Modifier.height(8.dp)); DetailLabel("ترکیب‌های رایج")
                    Text(word.collocations, color = Color(0xFF18243A), fontSize = 13.sp, lineHeight = 21.sp)
                }
                if (word.synonymsAntonyms.isNotBlank()) {
                    Spacer(Modifier.height(8.dp)); DetailLabel("مترادف‌ها و متضادها")
                    Text(word.synonymsAntonyms, color = Color(0xFF18243A), fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp)); DetailLabel("دسته‌بندی")
                Text(word.category, color = Color(0xFF718096), fontSize = 12.sp, lineHeight = 19.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoTag(label: String, tint: Color) {
    Box(Modifier.clip(RoundedCornerShape(30.dp)).background(tint.copy(alpha = .12f)).padding(horizontal = 11.dp, vertical = 6.dp)) {
        Text(label, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailLabel(label: String) {
    Text(label, color = Color(0xFF14213D), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(5.dp))
}
