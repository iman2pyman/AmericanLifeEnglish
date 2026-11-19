package com.ieamericanenglish.app

import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val Navy = Color(0xFF14213D)
private val Blue = Color(0xFF2563EB)
private val Teal = Color(0xFF0F9D91)
private val Gold = Color(0xFFF2B84B)
private val Ink = Color(0xFF18243A)
private val Muted = Color(0xFF718096)
private val Page = Color(0xFFF4F7FC)

class EntryActivity : ComponentActivity() {
    private lateinit var speech: AmericanSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speech = AmericanSpeech(this)
        setContent { MaterialTheme { AmericanEnglishRoot(onSpeak = { speech.speak(it) }, onExit = { finish() }) } }
    }
    override fun onDestroy() {
        if (::speech.isInitialized) speech.close()
        super.onDestroy()
    }
}

@Composable
private fun AmericanEnglishRoot(onSpeak: (String) -> Unit, onExit: () -> Unit) {
    val context = LocalContext.current
    val words = remember { loadVocabulary(context) }
    val learningStore = remember(context) { LearningStore(context) }
    val preferences = remember(context) { context.getSharedPreferences("american_english_settings", android.content.Context.MODE_PRIVATE) }
    var darkMode by remember { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
    var welcomeVisible by remember { mutableStateOf(!preferences.getBoolean("hide_welcome", false)) }
    var storeRevision by remember { mutableIntStateOf(0) }
    val bookmarkedWords = remember(storeRevision) { learningStore.bookmarks() }
    var selectedLevel by remember { mutableStateOf(learningStore.selectedLevel()) }
    var page by remember { mutableStateOf("home") }
    var selectedWord by remember { mutableStateOf<VocabularyWord?>(null) }
    var browsedLevel by remember { mutableStateOf("A1") }
    var dialog by remember { mutableStateOf<String?>(null) }
    var lastBackPressedAt by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = true) {
        when {
            welcomeVisible -> welcomeVisible = false
            dialog != null -> dialog = null
            selectedWord != null -> selectedWord = null
            page == "vocabulary-level-words" -> page = "vocabulary-level"
            page == "vocabulary-level" -> page = "vocabulary"
            page == "vocabulary-practice" -> page = "vocabulary"
            page == "vocabulary" -> page = "home"
            page != "home" -> page = "home"
            else -> {
                val now = SystemClock.elapsedRealtime()
                if (now - lastBackPressedAt <= 2_000L) {
                    onExit()
                } else {
                    lastBackPressedAt = now
                    Toast.makeText(
                        context,
                        "برای خروج دوباره دکمه بازگشت را بزنید",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    MaterialTheme(colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl, LocalAppDarkMode provides darkMode) {
        Surface(Modifier.fillMaxSize(), color = appColor(Page)) {
            when {
                selectedWord != null -> WordDetailScreen(
                    selectedWord!!, onSpeak, { selectedWord = null },
                    isBookmarked = LearningStore.wordId(selectedWord!!.word) in bookmarkedWords,
                    onToggleBookmark = { learningStore.toggleBookmark(selectedWord!!.word); storeRevision++ }
                )
                page == "home" -> HomeScreen(
                    onSearch = { page = "search" }, onVocabulary = { page = "vocabulary" },
                    onGrammar = { page = "grammar" }, onHelp = { dialog = "help" }, onAbout = { dialog = "about" },
                    darkMode = darkMode, onToggleDarkMode = {
                        darkMode = !darkMode
                        preferences.edit().putBoolean("dark_mode", darkMode).apply()
                    }
                )
                page == "search" -> SearchScreen(
                    words, onSpeak, { page = "home" }, { selectedWord = it },
                    bookmarks = bookmarkedWords,
                    onToggleBookmark = { learningStore.toggleBookmark(it); storeRevision++ }
                )
                page == "vocabulary" -> LearningHubScreen(
                    selectedLevel = selectedLevel,
                    wordCount = words.distinctBy { LearningStore.wordId(it.word) }.size,
                    onBack = { page = "home" },
                    onLevels = { page = "vocabulary-level" },
                    onPractice = { page = "vocabulary-practice" }
                )
                page == "vocabulary-level" -> VocabularyLevelScreen(
                    words = words,
                    onLevel = { browsedLevel = it; page = "vocabulary-level-words" },
                    onBack = { page = "vocabulary" }
                )
                page == "vocabulary-level-words" -> VocabularyLevelWordsScreen(
                    words = words, level = browsedLevel,
                    onWord = { selectedWord = it }, onSpeak = onSpeak,
                    onBack = { page = "vocabulary-level" },
                    bookmarks = bookmarkedWords,
                    onToggleBookmark = { learningStore.toggleBookmark(it); storeRevision++ }
                )
                page == "vocabulary-practice" -> LearningScreen(
                    words, onSpeak, { page = "vocabulary" }, { selectedWord = it },
                    initialLevel = selectedLevel,
                    onLevelChanged = { selectedLevel = it; learningStore.setSelectedLevel(it) },
                    bookmarks = bookmarkedWords,
                    onToggleBookmark = { learningStore.toggleBookmark(it); storeRevision++ },
                    learningStore = learningStore
                )
                else -> GrammarScreen { page = "home" }
            }
        }
        if (welcomeVisible) {
            WelcomeDialog(
                version = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "نامشخص",
                onClose = { welcomeVisible = false },
                onNeverShow = {
                    preferences.edit().putBoolean("hide_welcome", true).apply()
                    welcomeVisible = false
                }
            )
        }
        dialog?.let { kind ->
            val help = kind == "help"
            InfoDialog(
                if (help) "راهنمای برنامه" else "درباره برنامه و سازنده",
                if (help) "در جستجوی واژگان می‌توانید واژه را به انگلیسی یا معنی فارسی پیدا کنید. در آموزش واژگان، سطح CEFR را انتخاب کنید و واژه‌ها را همراه با معنی، تعریف، مثال و تلفظ یاد بگیرید. با لمس آیکون بلندگو، تلفظ انگلیسی آمریکایی از طریق موتور گفتار گوشی پخش می‌شود. در بخش گرامر، درس‌های سطح‌بندی‌شده را بخوانید و با آزمون کوتاه هر درس تمرین کنید."
                else """American Life English
برنامه‌ای برای یادگیری انگلیسی آمریکایی با تمرکز بر واژگان کاربردی زندگی، کار و تحصیل.

سازنده: imanmohammadpur

منابع و مراجع واژگان:
• فهرست‌های واژگانی Oxford 3000 و Oxford 5000 برای انتخاب و اولویت‌بندی بخشی از مدخل‌ها
• فهرست‌های نامزد واژگان برگرفته از منابع آموزشی Longman برای تکمیل و مقایسه
• چارچوب CEFR (از A1 تا C2) برای سطح‌بندی آموزشی واژه‌ها

منابع و ابزارهای کمکی:
• آوانگاری IPA با تمرکز بر تلفظ آمریکایی (General American)
• موتور گفتار Android Text-to-Speech با زبان English (United States) برای شنیدن تلفظ
• داده‌های آموزشی تدوین‌شده شامل معنی فارسی، تعریف انگلیسی، مثال، مترادف، خانواده واژگانی و هم‌آیندها

توضیح: فهرست‌های واژگان و چارچوب CEFR نقش مرجع انتخاب و سطح‌بندی دارند. معنی‌ها، تعریف‌ها و مثال‌های فارسی و انگلیسی در بانک برنامه به‌صورت آموزشی تدوین شده‌اند و همه مدخل‌ها هنوز بازبینی مستقل فرهنگ‌لغتی نشده‌اند.""",
                { dialog = null }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    onSearch: () -> Unit, onVocabulary: () -> Unit, onGrammar: () -> Unit,
    onHelp: () -> Unit, onAbout: () -> Unit, darkMode: Boolean, onToggleDarkMode: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(appColor(Page)), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth().padding(end = 14.dp, top = 6.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onToggleDarkMode) {
                Text(if (darkMode) "☀️ حالت روز" else "🌙 تم شب", color = appColor(Blue), fontWeight = FontWeight.Bold)
            }
        }
        Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Navy, appColor(Color(0xFF203D68))))).padding(horizontal = 22.dp, vertical = 30.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(appColor(Color.White).copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                        Text("A", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("AMERICAN ENGLISH", color = appColor(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text("انگلیسی را کاربردی یاد بگیر", color = appColor(Color.White), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(9.dp))
                Text("واژگان و مهارت‌های زبان انگلیسی آمریکایی", color = appColor(Color(0xFFD7E4F8)), fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("LEARN", Gold); Pill("PRACTICE", appColor(Color(0xFF78D5C8))); Pill("GROW", appColor(Color(0xFFB6C9FF)))
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(23.dp))
            Text("از کجا شروع می‌کنی؟", color = appColor(Ink), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("بخش موردنظرت را انتخاب کن", color = appColor(Muted), fontSize = 13.sp)
            Spacer(Modifier.height(19.dp))
            ActionCard("جستجوی واژگان", "واژه موردنظرت را سریع پیدا کن", "⌕", Blue, onSearch)
            Spacer(Modifier.height(12.dp))
            ActionCard("آموزش واژگان", "یادگیری واژه‌ها به‌صورت هدفمند", "✦", Teal, onVocabulary)
            Spacer(Modifier.height(12.dp))
            ActionCard("آموزش گرامر", "قواعد زبان را قدم‌به‌قدم یاد بگیر", "文", appColor(Color(0xFF8B5CF6)), onGrammar)
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = appColor(Color(0xFFE1E7F0)))
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onHelp) { Text("راهنمای برنامه", color = appColor(Blue), fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                Text("•", color = appColor(Color(0xFFB3BDCC)))
                TextButton(onClick = onAbout) { Text("درباره برنامه و سازنده", color = appColor(Blue), fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun Pill(label: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(30.dp)).background(appColor(Color.White).copy(alpha = .11f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, symbol: String, accent: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().height(82.dp).shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp), color = appColor(Color.White), tonalElevation = 1.dp) {
        Row(Modifier.fillMaxSize().padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Text(symbol, color = accent, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = appColor(Ink), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = appColor(Muted), fontSize = 11.sp)
            }
            Box(Modifier.size(29.dp).clip(CircleShape).background(accent.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                Text("‹", color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
private fun WelcomeDialog(version: String, onClose: () -> Unit, onNeverShow: () -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(.90f), shape = RoundedCornerShape(24.dp), color = appColor(Color.White), shadowElevation = 20.dp) {
            Column(Modifier.padding(21.dp).heightIn(max = 620.dp).verticalScroll(rememberScrollState())) {
                Text("به American Life English خوش آمدید", color = appColor(Navy), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
                Text("یادگیری انگلیسی آمریکایی برای زندگی، کار و تحصیل", color = appColor(Blue), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Text("سازنده: ایمان محمدپور", color = appColor(Ink), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("نسخه: $version", color = appColor(Muted), fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                Text("امکانات برنامه", color = appColor(Navy), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("• جستجوی واژه‌ها با کلمه انگلیسی یا معنی فارسی\n• آموزش واژگان در شش سطح CEFR از A1 تا C2\n• معنی فارسی، تلفظ آمریکایی، تعریف انگلیسی، مثال، مترادف و خانواده واژگانی\n• تمرین روزانه، مرور زمان‌بندی‌شده و ثبت پیشرفت به‌صورت آفلاین\n• آموزش گرامر سطح‌بندی‌شده همراه با مثال و تمرین", color = appColor(Ink), fontSize = 13.sp, lineHeight = 23.sp)
                Spacer(Modifier.height(12.dp))
                Text("نحوه استفاده", color = appColor(Navy), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("از صفحه اصلی، بخش موردنظر را انتخاب کنید. برای دیدن واژه‌های هر سطح، سطح موردنظر را لمس کنید. در تمرین روزانه سطح خود را انتخاب کرده و پس از مطالعه هر کارت، میزان یادآوری را مشخص کنید. برای شنیدن تلفظ، آیکون بلندگو را لمس کنید.", color = appColor(Ink), fontSize = 13.sp, lineHeight = 23.sp)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onClose, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("بستن") }
                Spacer(Modifier.height(7.dp))
                OutlinedButton(onClick = onNeverShow, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("دیگر نمایش نده") }
            }
        }
    }
}

@Composable
private fun InfoDialog(title: String, body: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(.88f), shape = RoundedCornerShape(24.dp), color = appColor(Color.White), shadowElevation = 18.dp) {
            Column(Modifier.padding(22.dp)) {
                Text(title, color = appColor(Navy), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(13.dp))
                Text(body, color = appColor(Ink), fontSize = 14.sp, lineHeight = 25.sp)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onDismiss, Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Text("متوجه شدم", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
