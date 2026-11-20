package com.ieamericanenglish.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale


private data class RelatedVocabularyTerms(val synonyms: List<String>, val antonyms: List<String>)

private fun splitRelatedTerms(value: Any?): List<String> {
    val raw = when (value) {
        null, JSONObject.NULL -> ""
        is JSONArray -> (0 until value.length()).mapNotNull { value.optString(it).takeIf(String::isNotBlank) }.joinToString(",")
        else -> value.toString()
    }
    return raw.trim().removePrefix("[").removeSuffix("]")
        .split(Regex("[,;|،؛]+"))
        .map { it.trim().trim('"', '\'') }
        .filter { it.isNotBlank() && it !in setOf("—", "-", "–", "none", "n/a") }
        .distinctBy { it.lowercase(Locale.US) }
}

private fun parseRelatedVocabularyTerms(raw: String): RelatedVocabularyTerms {
    if (raw.isBlank()) return RelatedVocabularyTerms(emptyList(), emptyList())
    val trimmed = raw.trim()
    if (trimmed.startsWith("{")) {
        runCatching {
            val json = JSONObject(trimmed)
            return RelatedVocabularyTerms(
                splitRelatedTerms(json.opt("synonyms") ?: json.opt("synonym")),
                splitRelatedTerms(json.opt("antonyms") ?: json.opt("antonym"))
            )
        }
    }
    val sides = trimmed.split(Regex("\\s*/\\s*"), limit = 2)
    return RelatedVocabularyTerms(
        splitRelatedTerms(sides.firstOrNull()),
        splitRelatedTerms(sides.getOrNull(1))
    )
}

private fun normalizeRelatedTerm(value: String): String = value.trim()
    .lowercase(Locale.US)
    .replace('’', '\'')
    .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
    .trim()
    .replace(Regex("\\s+"), " ")

private fun normalizeSearchText(value: String): String = value.lowercase()
    .replace('ي', 'ی').replace('ك', 'ک').replace('ى', 'ی')

private val LevelColors = mapOf(
    "A1" to Color(0xFF0F9D91), "A2" to Color(0xFF2563EB),
    "B1" to Color(0xFF8B5CF6), "B2" to Color(0xFFB7791F),
    "C1" to Color(0xFFDB6B35), "C2" to Color(0xFFBE4778)
)

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack) { Text("→ بازگشت", color = appColor(Color(0xFF2563EB)), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(8.dp))
        Text(title, color = appColor(Color(0xFF14213D)), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SearchScreen(words: List<VocabularyWord>, onSpeak: (String) -> Unit, onBack: () -> Unit, onWord: (VocabularyWord) -> Unit, bookmarks: Set<String> = emptySet(), onToggleBookmark: (String) -> Unit = {}) {
    var query by remember { mutableStateOf("") }
    val uniqueWordCount = remember(words) {
        words.map { it.word.trim().lowercase() }.distinct().size
    }
    val results = remember(query, words) {
        if (query.isBlank()) emptyList()
        else {
            val needle = normalizeSearchText(query.trim())
            words.filter {
                normalizeSearchText(it.word).contains(needle) ||
                    normalizeSearchText(it.meaningsFa).contains(needle)
            }
        }
    }
    Column(Modifier.fillMaxSize().background(appColor(Color(0xFFF4F7FC))).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("جستجوی واژگان", onBack)
        Text("کل بانک آموزشی: $uniqueWordCount واژه", color = appColor(Color(0xFF2563EB)), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, label = { Text("کلمه انگلیسی یا معنی فارسی") },
            placeholder = { Text("مثلاً: work یا کار") }, shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = appColor(Color(0xFF2563EB)), focusedLabelColor = appColor(Color(0xFF2563EB)))
        )
        Spacer(Modifier.height(10.dp))
        Text(if (query.isBlank()) "برای جستجو، واژه انگلیسی یا معنی فارسی را وارد کنید" else results.size.toString() + " نتیجه", color = appColor(Color(0xFF718096)), fontSize = 12.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
            items(results, key = { it.word }) { word -> WordRow(word, onSpeak, isBookmarked = LearningStore.wordId(word.word) in bookmarks, onToggleBookmark = { onToggleBookmark(word.word) }) { onWord(word) } }
        }
    }
}

@Composable
fun LearningHubScreen(
    selectedLevel: String,
    wordCount: Int,
    onBack: () -> Unit,
    onLevels: () -> Unit,
    onPractice: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(appColor(Color(0xFFF4F7FC)))
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader("آموزش واژگان", onBack)
        Spacer(Modifier.height(18.dp))
        Text("یادگیری را از کجا شروع کنیم؟", color = appColor(Color(0xFF14213D)), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(7.dp))
        Text("یکی از دو بخش زیر را انتخاب کن", color = appColor(Color(0xFF718096)), fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        LearningEntryCard(
            title = "۱. سطح‌بندی لغات",
            subtitle = "انتخاب و مشاهده واژه‌ها بر اساس سطح A1 تا C2",
            detail = "سطح فعلی: $selectedLevel  •  $wordCount واژه در بانک",
            accent = appColor(Color(0xFF2563EB)),
            symbol = "A–Z",
            onClick = onLevels
        )
        Spacer(Modifier.height(16.dp))
        LearningEntryCard(
            title = "۲. آموزش و تمرین لغات",
            subtitle = "برنامه روزانه، فلش‌کارت و مرور زمان‌بندی‌شده",
            detail = "هدف روزانه ۱۵ واژه • هدف ماهانه ۴۵۰ واژه",
            accent = appColor(Color(0xFF0F9D91)),
            symbol = "✦",
            onClick = onPractice
        )
        Spacer(Modifier.height(22.dp))
        Text(
            "پیشرفت و یادگیری شما به‌صورت آفلاین روی همین گوشی ذخیره می‌شود.",
            color = appColor(Color(0xFF718096)), fontSize = 12.sp, lineHeight = 20.sp
        )
    }
}

@Composable
private fun LearningEntryCard(
    title: String,
    subtitle: String,
    detail: String,
    accent: Color,
    symbol: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
        shape = RoundedCornerShape(22.dp),
        color = appColor(Color.White),
        shadowElevation = 3.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(54.dp).clip(RoundedCornerShape(17.dp)).background(accent.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, color = accent, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = appColor(Color(0xFF14213D)), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(5.dp))
                Text(subtitle, color = appColor(Color(0xFF4A5568)), fontSize = 12.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(6.dp))
                Text(detail, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("‹", color = accent, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VocabularyLevelScreen(words: List<VocabularyWord>, onLevel: (String) -> Unit, onBack: () -> Unit) {
    val uniqueWords = remember(words) { words.distinctBy { LearningStore.wordId(it.word) } }
    val counts = remember(uniqueWords) { uniqueWords.groupingBy { it.cefr.uppercase() }.eachCount() }
    Column(Modifier.fillMaxSize().background(appColor(Color(0xFFF4F7FC))).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("سطح‌بندی لغات", onBack)
        Text("سطح موردنظرت را انتخاب کن", color = appColor(Color(0xFF18243A)), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("با لمس هر سطح، صفحه اختصاصی واژگان همان سطح باز می‌شود.", color = appColor(Color(0xFF718096)), fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { item ->
            val tint = appColor(LevelColors[item] ?: Color(0xFF2563EB))
            Surface(onClick = { onLevel(item) }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(18.dp), color = appColor(Color.White), shadowElevation = 2.dp) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(tint), contentAlignment = Alignment.Center) { Text(item, color = appColor(Color.White), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("سطح $item", color = appColor(Color(0xFF18243A)), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${counts[item] ?: 0} واژه در این سطح", color = appColor(Color(0xFF718096)), fontSize = 12.sp)
                    }
                    Text("مشاهده واژه‌ها  ←", color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VocabularyLevelWordsScreen(words: List<VocabularyWord>, level: String, listState: LazyListState, onWord: (VocabularyWord) -> Unit, onSpeak: (String) -> Unit, onBack: () -> Unit, bookmarks: Set<String> = emptySet(), onToggleBookmark: (String) -> Unit = {}) {
    var query by remember(level) { mutableStateOf("") }
    val levelWords = remember(words, level) { words.distinctBy { LearningStore.wordId(it.word) }.filter { it.cefr.equals(level, true) }.sortedBy { it.word.lowercase() } }
    val results = remember(levelWords, query) {
        if (query.isBlank()) levelWords else {
            val needle = normalizeSearchText(query.trim())
            levelWords.filter { normalizeSearchText(it.word).contains(needle) || normalizeSearchText(it.meaningsFa).contains(needle) }
        }
    }
    val tint = appColor(LevelColors[level] ?: Color(0xFF2563EB))
    Column(Modifier.fillMaxSize().background(appColor(Color(0xFFF4F7FC))).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("واژگان سطح $level", onBack)
        Text("${levelWords.size} واژه در این سطح", color = tint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("جستجو در واژه‌های $level") }, placeholder = { Text("واژه انگلیسی یا معنی فارسی") }, shape = RoundedCornerShape(15.dp))
        Spacer(Modifier.height(10.dp))
        Text(if (query.isBlank()) "همه واژه‌ها" else "${results.size} نتیجه", color = appColor(Color(0xFF718096)), fontSize = 11.sp)
        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (levelWords.isEmpty()) "برای این سطح هنوز واژه‌ای ثبت نشده است." else "واژه‌ای با این جستجو پیدا نشد.", color = appColor(Color(0xFF718096)), fontSize = 13.sp) }
        } else {
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(results, key = { LearningStore.wordId(it.word) }) { word ->
                    WordRow(word, onSpeak, isBookmarked = LearningStore.wordId(word.word) in bookmarks, onToggleBookmark = { onToggleBookmark(word.word) }) { onWord(word) }
                }
            }
        }
    }
}
@Composable
fun LearningScreen(words: List<VocabularyWord>, onSpeak: (String) -> Unit, onBack: () -> Unit, onWord: (VocabularyWord) -> Unit, initialLevel: String = "A1", onLevelChanged: (String) -> Unit = {}, bookmarks: Set<String> = emptySet(), onToggleBookmark: (String) -> Unit = {}, learningStore: LearningStore? = null) {
    var level by remember { mutableStateOf(initialLevel) }
    LaunchedEffect(initialLevel) { level = initialLevel }
    var plan by remember { mutableStateOf<DailyLearningPlan?>(null) }
    var queue by remember { mutableStateOf<List<VocabularyWord>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var restored by remember { mutableStateOf(false) }

    LaunchedEffect(level, words, learningStore) {
        plan = learningStore?.buildDailyPlan(words, level)
        val saved = learningStore?.restorePracticeSession(words, level)
        if (saved != null && saved.first.isNotEmpty()) {
            queue = saved.first
            index = saved.second.coerceIn(0, saved.first.size)
            finished = index >= queue.size
            restored = true
        } else {
            queue = emptyList(); index = 0; finished = false; restored = false
        }
    }
    val current = queue.getOrNull(index)
    val isNew = current != null && plan?.newWords?.any { LearningStore.wordId(it.word) == LearningStore.wordId(current.word) } == true
    val tint = appColor(LevelColors[level] ?: Color(0xFF2563EB))
    val done = plan?.completedToday ?: 0

    Column(Modifier.fillMaxSize().background(appColor(Color(0xFFF4F7FC))).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("تمرین روزانه واژگان", onBack)
        Text("سطح یادگیری", color = appColor(Color(0xFF18243A)), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { item ->
                val color = appColor(LevelColors[item] ?: tint)
                FilterChip(selected = level == item, onClick = {
                    level = item; onLevelChanged(item); queue = emptyList(); index = 0; finished = false; learningStore?.clearPracticeSession()
                }, label = { Text(item, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color, selectedLabelColor = appColor(Color.White)))
            }
        }
        plan?.let { p ->
            Spacer(Modifier.height(12.dp))
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = appColor(Color.White), shadowElevation = 3.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text("برنامه یادگیری امروز", color = appColor(Color(0xFF14213D)), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text("ابتدا مرورهای موعددار، سپس واژه‌های تازه", color = appColor(Color(0xFF718096)), fontSize = 11.sp)
                    Spacer(Modifier.height(13.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PracticeMetric("مرور موعددار", p.reviewWords.size.toString(), appColor(Color(0xFFB7791F)), Modifier.weight(1f))
                        PracticeMetric("واژه جدید", p.newWords.size.toString(), appColor(Color(0xFF0F9D91)), Modifier.weight(1f))
                        PracticeMetric("هدف روزانه", "15", appColor(Color(0xFF2563EB)), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("پیشرفت واژه‌های جدید", modifier = Modifier.weight(1f), color = appColor(Color(0xFF4A5568)), fontSize = 12.sp)
                        Text("$done / 15", color = tint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    LinearProgressIndicator(progress = { (done / 15f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(8.dp).clip(CircleShape), color = appColor(Color(0xFF0F9D91)), trackColor = appColor(Color(0xFFE8EEF6)))
                    Text("پیشرفت ماهانه: ${p.monthCompleted} از ${p.monthlyNewWordGoal}", color = appColor(Color(0xFF718096)), fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp))
                    if (p.reviewBacklogCount > 0) Text("${p.reviewBacklogCount} مرور دیگر موعددار است و به‌تدریج در برنامه روزهای بعد قرار می‌گیرد؛ سقف مرور روزانه ۳۰ واژه است.", color = appColor(Color(0xFFB7791F)), fontSize = 11.sp, lineHeight = 18.sp)
                    if (p.bookmarkedWaiting > 0) Text("${p.bookmarkedWaiting} واژه نشان‌دار در صف روزهای آینده است.", color = appColor(Color(0xFFB7791F)), fontSize = 11.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        queue = (p.reviewWords + p.newWords).distinctBy { LearningStore.wordId(it.word) }
                        index = 0; finished = queue.isEmpty(); restored = false
                        learningStore?.savePracticeSession(level, queue, 0)
                    }, enabled = p.reviewWords.isNotEmpty() || p.newWords.isNotEmpty(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                        Text(if (queue.isNotEmpty() && !finished) "شروع دوباره جلسه" else "شروع تمرین امروز")
                    }
                    if (restored && queue.isNotEmpty() && !finished) Text("جلسه قبلی بازیابی شد؛ از واژه ${index + 1} ادامه می‌دهی.", color = appColor(Color(0xFF0F9D91)), fontSize = 11.sp)
                }
            }
        }
        if (current != null) {
            Spacer(Modifier.height(14.dp))
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(21.dp), color = appColor(Color.White), shadowElevation = 4.dp) {
                Column(Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isNew) "واژه جدید" else "مرور زمان‌بندی‌شده", modifier = Modifier.weight(1f), color = if (isNew) appColor(Color(0xFF0F9D91)) else appColor(Color(0xFFB7791F)), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${index + 1} از ${queue.size}", color = appColor(Color(0xFF718096)), fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(current.word, color = appColor(LevelColors[current.cefr] ?: tint), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                            Text(current.ipaUs.ifBlank { "تلفظ ثبت نشده" }, color = appColor(Color(0xFF718096)), fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(current.cefr, color = tint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            TextButton(onClick = { onSpeak(current.word) }) { Text("🔊 تلفظ آمریکایی", fontSize = 11.sp) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (current.partOfSpeech.isNotBlank()) InfoTag(current.partOfSpeech, appColor(Color(0xFF2563EB)))
                        if (current.category.isNotBlank()) InfoTag(current.category, appColor(Color(0xFF0F9D91)))
                    }
                    Spacer(Modifier.height(12.dp))
                    DetailLabel("معنی فارسی")
                    Text(current.meaningsFa.ifBlank { "معنی فارسی ثبت نشده است." }, color = appColor(Color(0xFF18243A)), fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
                    Spacer(Modifier.height(11.dp))
                    DetailLabel("تعریف انگلیسی")
                    Text(current.definitionEn.ifBlank { "Definition not available." }, color = appColor(Color(0xFF4A5568)), fontSize = 13.sp, lineHeight = 22.sp)
                    Spacer(Modifier.height(11.dp))
                    DetailLabel("مثال کاربردی")
                    current.exampleEn.split("|").map { it.trim() }.filter { it.isNotBlank() }.forEach { Text("• $it", color = appColor(Color(0xFF2563EB)), fontSize = 13.sp, lineHeight = 22.sp) }
                    current.exampleFa.split("|").map { it.trim() }.filter { it.isNotBlank() }.forEach { Text(it, color = appColor(Color(0xFF718096)), fontSize = 12.sp, lineHeight = 21.sp) }
                    if (current.collocations.isNotBlank()) {
                        Spacer(Modifier.height(11.dp)); DetailLabel("ترکیب‌های رایج")
                        Text(current.collocations, color = appColor(Color(0xFF18243A)), fontSize = 12.sp, lineHeight = 21.sp)
                    }
                    if (current.wordFamily.isNotBlank() || current.synonymsAntonyms.isNotBlank()) {
                        TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "بستن جزئیات تکمیلی" else "خانواده واژگانی و مترادف‌ها") }
                        if (expanded) {
                            if (current.wordFamily.isNotBlank()) { DetailLabel("خانواده واژگانی"); Text(current.wordFamily, color = appColor(Color(0xFF18243A)), fontSize = 12.sp, lineHeight = 21.sp) }
                            if (current.synonymsAntonyms.isNotBlank()) { Spacer(Modifier.height(7.dp)); DetailLabel("مترادف‌ها و متضادها"); Text(current.synonymsAntonyms, color = appColor(Color(0xFF18243A)), fontSize = 12.sp, lineHeight = 21.sp) }
                        }
                    }
                    HorizontalDivider(color = appColor(Color(0xFFE5EAF2)), modifier = Modifier.padding(vertical = 10.dp))
                    Text("بعد از مطالعه، میزان یادآوری خودت را انتخاب کن:", color = appColor(Color(0xFF14213D)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(Triple("again", "فراموش", appColor(Color(0xFFBE4778))), Triple("hard", "سخت", appColor(Color(0xFFB7791F))), Triple("good", "خوب", appColor(Color(0xFF2563EB))), Triple("easy", "آسان", appColor(Color(0xFF0F9D91)))).forEach { (rating, label, color) ->
                            OutlinedButton(onClick = {
                                learningStore?.recordAnswer(current.word, rating)
                                if (isNew) learningStore?.markDailyWordCompleted(current.word, level)
                                val next = index + 1
                                index = next
                                if (next < queue.size) learningStore?.savePracticeSession(level, queue, next)
                                else { finished = true; learningStore?.clearPracticeSession() }
                                plan = learningStore?.buildDailyPlan(words, level)
                                expanded = false
                            }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = color)) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    TextButton(onClick = { onToggleBookmark(current.word) }, modifier = Modifier.align(Alignment.End)) {
                        Text(if (LearningStore.wordId(current.word) in bookmarks) "★ نشان‌دار" else "☆ افزودن به نشان‌دارها")
                    }
                    TextButton(onClick = { onWord(current) }, modifier = Modifier.align(Alignment.Start)) { Text("مشاهده صفحه کامل واژه") }
                }
            }
        } else if (finished) {
            Spacer(Modifier.height(14.dp))
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = appColor(Color.White)) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("جلسه امروز تمام شد", color = appColor(Color(0xFF14213D)), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${queue.size} واژه مرور شد. مرور بعدی طبق زمان‌بندی ثبت شد.", color = appColor(Color(0xFF0F9D91)), fontSize = 13.sp, lineHeight = 22.sp)
                    Button(onClick = { queue = emptyList(); index = 0; finished = false }) { Text("بازگشت به برنامه") }
                }
            }
        }
    }
}

@Composable
private fun PracticeMetric(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(13.dp), color = tint.copy(alpha = .09f)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
            Text(value, color = tint, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = appColor(Color(0xFF4A5568)), fontSize = 10.sp)
        }
    }
}

@Composable
fun WordRow(word: VocabularyWord, onSpeak: (String) -> Unit, isBookmarked: Boolean = false, onToggleBookmark: () -> Unit = {}, onClick: () -> Unit) {
    val accent = appColor(LevelColors[word.cefr] ?: Color(0xFF2563EB))
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = appColor(Color.White), shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(horizontal = 13.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(word.word, color = accent, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    Text(word.ipaUs, color = appColor(Color(0xFF718096)), fontSize = 11.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(word.meaningsFa, color = appColor(Color(0xFF18243A)), fontSize = 12.sp, maxLines = 2)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(word.cefr, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onToggleBookmark, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(30.dp)) { Text(if (isBookmarked) "★" else "☆", color = if (isBookmarked) appColor(Color(0xFFB7791F)) else appColor(Color(0xFF718096)), fontSize = 20.sp) }
                TextButton(onClick = { onSpeak(word.word) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) {
                    Text("🔊", fontSize = 17.sp)
                }
            }
        }
    }
}

@Composable
fun WordDetailScreen(word: VocabularyWord, onSpeak: (String) -> Unit, onBack: () -> Unit, isBookmarked: Boolean = false, onToggleBookmark: () -> Unit = {}, allWords: List<VocabularyWord> = emptyList(), onRelatedWord: (VocabularyWord) -> Unit = {}) {
    val accent = LevelColors[word.cefr] ?: appColor(Color(0xFF2563EB))
    val englishExamples = word.exampleEn.split("|").map { it.trim() }.filter { it.isNotBlank() }
    val persianExamples = word.exampleFa.split("|").map { it.trim() }.filter { it.isNotBlank() }
    val relationTerms = remember(word.synonymsAntonyms) { parseRelatedVocabularyTerms(word.synonymsAntonyms) }
    // Match related terms after normalizing punctuation, hyphens and whitespace.
    // This lets entries such as "one-of-a-kind" match "one of a kind".
    val vocabularyIndex = remember(allWords) {
        allWords.distinctBy { LearningStore.wordId(it.word) }
            .associateBy { normalizeRelatedTerm(it.word) }
    }
    val compactVocabularyIndex = remember(allWords) {
        allWords.distinctBy { LearningStore.wordId(it.word) }
            .associateBy { normalizeRelatedTerm(it.word).replace(" ", "") }
    }
    fun findVocabularyTerm(term: String): VocabularyWord? {
        val normalized = normalizeRelatedTerm(term)
        return vocabularyIndex[normalized] ?: compactVocabularyIndex[normalized.replace(" ", "")]
    }
    val linkedSynonyms = remember(relationTerms, vocabularyIndex, compactVocabularyIndex, word.word) {
        relationTerms.synonyms.mapNotNull { findVocabularyTerm(it) }
            .filter { LearningStore.wordId(it.word) != LearningStore.wordId(word.word) }
            .distinctBy { LearningStore.wordId(it.word) }
    }
    val unlinkedSynonyms = remember(relationTerms, vocabularyIndex, compactVocabularyIndex) {
        relationTerms.synonyms.filter { findVocabularyTerm(it) == null }
    }
    Column(Modifier.fillMaxSize().background(appColor(Color(0xFFF4F7FC))).verticalScroll(rememberScrollState()).padding(18.dp)) {
        ScreenHeader("اطلاعات واژه", onBack)
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = appColor(Color.White), shadowElevation = 3.dp) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(word.word, color = accent, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                        Text(word.ipaUs, color = appColor(Color(0xFF718096)), fontSize = 15.sp)
                        if (word.pronunciationGuideFa.isNotBlank()) Text("راهنمای تلفظ: ${word.pronunciationGuideFa}", color = appColor(Color(0xFF0F766E)), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { onSpeak(word.word) }) { Text("🔊 تلفظ", color = appColor(Color(0xFF2563EB)), fontWeight = FontWeight.Bold) }
                        TextButton(onClick = onToggleBookmark) { Text(if (isBookmarked) "★ نشان‌دار" else "☆ نشان‌دار", color = if (isBookmarked) appColor(Color(0xFFB7791F)) else appColor(Color(0xFF2563EB)), fontWeight = FontWeight.Bold) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    InfoTag(word.cefr, accent)
                    InfoTag(word.partOfSpeech, appColor(Color(0xFF2563EB)))
                }
                Spacer(Modifier.height(16.dp))
                DetailLabel("معنی فارسی")
                Text(word.meaningsFa, color = appColor(Color(0xFF18243A)), fontSize = 15.sp, lineHeight = 27.sp)
                Spacer(Modifier.height(14.dp))
                DetailLabel("تعریف انگلیسی")
                Text(word.definitionEn, color = appColor(Color(0xFF18243A)), fontSize = 14.sp, lineHeight = 23.sp)
                Spacer(Modifier.height(14.dp))
                DetailLabel("مثال‌های کاربردی")
                englishExamples.forEachIndexed { index, example ->
                    Text("• " + example, color = appColor(Color(0xFF2563EB)), fontSize = 13.sp, lineHeight = 21.sp)
                    if (index < persianExamples.size) Text(persianExamples[index], color = appColor(Color(0xFF718096)), fontSize = 12.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(7.dp))
                }
                if (word.wordFamily.isNotBlank()) {
                    Spacer(Modifier.height(8.dp)); DetailLabel("خانواده واژگانی")
                    Text(word.wordFamily, color = appColor(Color(0xFF18243A)), fontSize = 13.sp, lineHeight = 21.sp)
                }
                if (word.collocations.isNotBlank()) {
                    Spacer(Modifier.height(8.dp)); DetailLabel("ترکیب‌های رایج")
                    Text(word.collocations, color = appColor(Color(0xFF18243A)), fontSize = 13.sp, lineHeight = 21.sp)
                }
                if (relationTerms.synonyms.isNotEmpty() || relationTerms.antonyms.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    if (relationTerms.synonyms.isNotEmpty()) {
                        DetailLabel("مترادف‌ها")
                        if (linkedSynonyms.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                linkedSynonyms.forEach { synonym ->
                                    Surface(
                                        onClick = { onRelatedWord(synonym) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = appColor(Color(0xFFEAF2FF))
                                    ) {
                                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(synonym.word, modifier = Modifier.weight(1f), color = appColor(Color(0xFF2563EB)), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("مشاهده واژه  ←", color = appColor(Color(0xFF2563EB)), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        if (unlinkedSynonyms.isNotEmpty()) {
                            Text(
                                "در بانک واژگان موجود نیست: " + unlinkedSynonyms.joinToString("، "),
                                color = appColor(Color(0xFF718096)), fontSize = 11.sp, lineHeight = 19.sp
                            )
                        }
                    }
                    if (relationTerms.antonyms.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        DetailLabel("متضادها")
                        Text(relationTerms.antonyms.joinToString("، "), color = appColor(Color(0xFF18243A)), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp)); DetailLabel("دسته‌بندی")
                Text(word.category, color = appColor(Color(0xFF718096)), fontSize = 12.sp, lineHeight = 19.sp)
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
    Text(label, color = appColor(Color(0xFF14213D)), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(5.dp))
}
