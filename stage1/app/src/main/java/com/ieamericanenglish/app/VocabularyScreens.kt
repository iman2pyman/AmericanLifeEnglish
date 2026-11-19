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
        TextButton(onClick = onBack) { Text("→ بازگشت", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color(0xFF14213D), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
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
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC)).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("جستجوی واژگان", onBack)
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, label = { Text("کلمه انگلیسی یا معنی فارسی") },
            placeholder = { Text("مثلاً: work یا کار") }, shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), focusedLabelColor = Color(0xFF2563EB))
        )
        Spacer(Modifier.height(10.dp))
        Text(if (query.isBlank()) "جستجو در " + uniqueWordCount + " واژه" else results.size.toString() + " نتیجه", color = Color(0xFF718096), fontSize = 12.sp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
            items(results, key = { it.word }) { word -> WordRow(word, onSpeak, isBookmarked = LearningStore.wordId(word.word) in bookmarks, onToggleBookmark = { onToggleBookmark(word.word) }) { onWord(word) } }
        }
    }
}

@Composable
fun LearningScreen(words: List<VocabularyWord>, onSpeak: (String) -> Unit, onBack: () -> Unit, onWord: (VocabularyWord) -> Unit, initialLevel: String = "A1", onLevelChanged: (String) -> Unit = {}, bookmarks: Set<String> = emptySet(), onToggleBookmark: (String) -> Unit = {}, learningStore: LearningStore? = null) {
    var level by remember { mutableStateOf(initialLevel) }
    LaunchedEffect(initialLevel) { level = initialLevel }
    val levelWords = remember(level, words) { words.filter { it.cefr.equals(level, true) }.distinctBy { LearningStore.wordId(it.word) } }
    var dailyPlan by remember { mutableStateOf<DailyLearningPlan?>(null) }
    LaunchedEffect(level, words, learningStore) {
        dailyPlan = learningStore?.buildDailyPlan(words, level)
    }
    val quizStats = remember(level, words, learningStore, quizAnswer, quizIndex) {
        learningStore?.quizStats(words, level)
    }
    val weakWords = remember(level, words, learningStore, quizAnswer, quizIndex) {
        learningStore?.weakWords(words, level).orEmpty()
    }
    var practiceQueue by remember { mutableStateOf<List<VocabularyWord>>(emptyList()) }
    var practiceIndex by remember { mutableIntStateOf(0) }
    var quizQuestions by remember { mutableStateOf<List<VocabularyQuizQuestion>>(emptyList()) }
    var quizIndex by remember { mutableIntStateOf(0) }
    var quizAnswer by remember { mutableStateOf<String?>(null) }
    var quizScore by remember { mutableIntStateOf(0) }
    var quizUnavailable by remember { mutableStateOf(false) }
    val currentPracticeWord = practiceQueue.getOrNull(practiceIndex)
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC)).padding(horizontal = 18.dp, vertical = 12.dp)) {
        ScreenHeader("آموزش واژگان", onBack)
        Text("سطح یادگیری را انتخاب کن", color = Color(0xFF18243A), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { item ->
                val tint = LevelColors[item] ?: Color(0xFF2563EB)
                FilterChip(
                    selected = level == item, onClick = { level = item; onLevelChanged(item) },
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
        dailyPlan?.let { plan ->
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
                Column(Modifier.padding(13.dp)) {
                    Text("برنامه امروز", color = Color(0xFF14213D), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(5.dp))
                    Text("${plan.newWords.size} واژه جدید • ${plan.reviewWords.size} مرور موعددار", color = Color(0xFF2563EB), fontSize = 12.sp)
                    Text("پیشرفت ماه: ${plan.monthCompleted} از ${plan.monthlyNewWordGoal} • هدف روزانه ۱۵ واژه", color = Color(0xFF718096), fontSize = 11.sp)
                    if (plan.bookmarkedWaiting > 0) Text("${plan.bookmarkedWaiting} واژه نشان‌دار در صف انتظار", color = Color(0xFFB7791F), fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { practiceQueue = plan.newWords + plan.reviewWords; practiceIndex = 0 },
                        enabled = plan.newWords.isNotEmpty() || plan.reviewWords.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    ) { Text("شروع تمرین امروز") }
                }
            }
        }
        quizStats?.let { stats ->
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
                Column(Modifier.padding(13.dp)) {
                    Text("عملکرد آزمون‌ها", color = Color(0xFF14213D), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        if (stats.answered == 0) "هنوز پاسخی ثبت نشده است."
                        else "درست: ${stats.correct} از ${stats.answered}  •  دقت: ${stats.accuracyPercent}٪",
                        color = Color(0xFF2563EB), fontSize = 12.sp
                    )
                    stats.byType.forEach { item ->
                        val title = QuizQuestionType.values().firstOrNull { it.name == item.type }?.titleFa ?: item.type
                        Text("$title: ${item.accuracyPercent}٪ (${item.correct}/${item.answered})",
                            color = Color(0xFF718096), fontSize = 11.sp)
                    }
                    if (weakWords.isNotEmpty()) {
                        Spacer(Modifier.height(7.dp))
                        Text("${weakWords.size} واژه نیازمند تمرین مجدد شناسایی شد.", color = Color(0xFFB7791F), fontSize = 12.sp)
                        Button(
                            onClick = { practiceQueue = weakWords; practiceIndex = 0; quizQuestions = emptyList() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("تمرین واژه‌های ضعیف") }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                quizQuestions = VocabularyQuizEngine.createQuiz(words, level, 10)
                quizUnavailable = quizQuestions.isEmpty()
                quizIndex = 0
                quizAnswer = null
                quizScore = 0
                practiceQueue = emptyList()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = levelWords.size >= 4,
            shape = RoundedCornerShape(12.dp)
        ) { Text("شروع آزمون هوشمند (تا ۱۰ سؤال)") }
        if (quizUnavailable) Text("برای این سطح داده کافی جهت ساخت سؤال معتبر وجود ندارد.", color = Color(0xFFBE4778), fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        if (quizQuestions.isNotEmpty()) {
            if (quizIndex >= quizQuestions.size) {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
                    Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("آزمون به پایان رسید", color = Color(0xFF14213D), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text("$quizScore از ${quizQuestions.size} پاسخ درست", color = Color(0xFF0F9D91), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("نتیجه‌ها به‌صورت آفلاین ذخیره شدند.", color = Color(0xFF718096), fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { quizQuestions = emptyList(); quizIndex = 0; quizAnswer = null }) { Text("بازگشت به آموزش") }
                    }
                }
            } else {
                val question = quizQuestions[quizIndex]
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Text("آزمون ${quizIndex + 1} از ${quizQuestions.size}  •  ${question.type.titleFa}", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Text(question.prompt, color = Color(0xFF14213D), fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 25.sp)
                        Spacer(Modifier.height(12.dp))
                        question.options.forEach { option ->
                            val chosen = quizAnswer == option
                            OutlinedButton(
                                onClick = {
                                    if (quizAnswer == null) {
                                        quizAnswer = option
                                        val correct = option == question.correctAnswer
                                        if (correct) quizScore++
                                        learningStore?.recordQuizAnswer(question.targetWord, question.type.name, option, question.correctAnswer, correct)
                                        learningStore?.recordAnswer(question.targetWord, if (correct) "good" else "again")
                                    }
                                },
                                enabled = quizAnswer == null,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                                shape = RoundedCornerShape(11.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (quizAnswer != null && option == question.correctAnswer) Color(0xFF0F9D91) else Color(0xFF26364D)
                                )
                            ) { Text(option, modifier = Modifier.fillMaxWidth()) }
                        }
                        if (quizAnswer != null) {
                            val correct = quizAnswer == question.correctAnswer
                            Text(if (correct) "آفرین، پاسخ درست است." else "پاسخ درست: ${question.correctAnswer}",
                                color = if (correct) Color(0xFF0F9D91) else Color(0xFFBE4778),
                                fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(question.explanation, color = Color(0xFF718096), fontSize = 12.sp, lineHeight = 20.sp)
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = { quizIndex++; quizAnswer = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (quizIndex + 1 == quizQuestions.size) "دیدن نتیجه" else "سؤال بعدی")
                            }
                        }
                    }
                }
            }
        } else if (currentPracticeWord != null) {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text("تمرین ${practiceIndex + 1} از ${practiceQueue.size}", color = Color(0xFF718096), fontSize = 12.sp)
                    Text(currentPracticeWord.word, color = LevelColors[currentPracticeWord.cefr] ?: Color(0xFF2563EB), fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    Text(currentPracticeWord.ipaUs, color = Color(0xFF718096), fontSize = 13.sp)
                    TextButton(onClick = { onSpeak(currentPracticeWord.word) }) { Text("🔊 شنیدن تلفظ") }
                    Divider(color = Color(0xFFE5EAF2))
                    Text(currentPracticeWord.meaningsFa, color = Color(0xFF18243A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text(currentPracticeWord.definitionEn, color = Color(0xFF4A5568), fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("این واژه را چقدر به خاطر داری؟", color = Color(0xFF14213D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("again" to "دوباره", "hard" to "سخت", "good" to "خوب", "easy" to "آسان").forEach { (rating, label) ->
                            OutlinedButton(onClick = {
                                learningStore?.recordAnswer(currentPracticeWord.word, rating)
                                if (dailyPlan?.newWords?.any { LearningStore.wordId(it.word) == LearningStore.wordId(currentPracticeWord.word) } == true) learningStore?.markDailyWordCompleted(currentPracticeWord.word)
                                if (practiceIndex + 1 < practiceQueue.size) practiceIndex++ else { practiceQueue = emptyList(); practiceIndex = 0 }
                            }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 2.dp, vertical = 7.dp)) { Text(label, fontSize = 11.sp) }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
                items(levelWords, key = { it.word }) { word -> WordRow(word, onSpeak, isBookmarked = LearningStore.wordId(word.word) in bookmarks, onToggleBookmark = { onToggleBookmark(word.word) }) { onWord(word) } }
            }
        }
    }
}

@Composable
fun WordRow(word: VocabularyWord, onSpeak: (String) -> Unit, isBookmarked: Boolean = false, onToggleBookmark: () -> Unit = {}, onClick: () -> Unit) {
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
                TextButton(onClick = onToggleBookmark, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(30.dp)) { Text(if (isBookmarked) "★" else "☆", color = if (isBookmarked) Color(0xFFB7791F) else Color(0xFF718096), fontSize = 20.sp) }
                TextButton(onClick = { onSpeak(word.word) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) {
                    Text("🔊", fontSize = 17.sp)
                }
            }
        }
    }
}

@Composable
fun WordDetailScreen(word: VocabularyWord, onSpeak: (String) -> Unit, onBack: () -> Unit, isBookmarked: Boolean = false, onToggleBookmark: () -> Unit = {}) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { onSpeak(word.word) }) { Text("🔊 تلفظ", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) }
                        TextButton(onClick = onToggleBookmark) { Text(if (isBookmarked) "★ نشان‌دار" else "☆ نشان‌دار", color = if (isBookmarked) Color(0xFFB7791F) else Color(0xFF2563EB), fontWeight = FontWeight.Bold) }
                    }
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
