package com.ieamericanenglish.app

import android.os.Bundle
import androidx.activity.ComponentActivity
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
        setContent { MaterialTheme { AmericanEnglishRoot(onSpeak = { speech.speak(it) }) } }
    }
    override fun onDestroy() {
        if (::speech.isInitialized) speech.close()
        super.onDestroy()
    }
}

@Composable
private fun AmericanEnglishRoot(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val words = remember { loadVocabulary(context) }
    var page by remember { mutableStateOf("home") }
    var selectedWord by remember { mutableStateOf<VocabularyWord?>(null) }
    var dialog by remember { mutableStateOf<String?>(null) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(Modifier.fillMaxSize(), color = Page) {
            when {
                selectedWord != null -> WordDetailScreen(selectedWord!!, onSpeak) { selectedWord = null }
                page == "home" -> HomeScreen(
                    onSearch = { page = "search" }, onVocabulary = { page = "vocabulary" },
                    onGrammar = { page = "grammar" }, onHelp = { dialog = "help" }, onAbout = { dialog = "about" }
                )
                page == "search" -> SearchScreen(words, onSpeak, { page = "home" }) { selectedWord = it }
                page == "vocabulary" -> LearningScreen(words, onSpeak, { page = "home" }) { selectedWord = it }
                else -> GrammarScreen { page = "home" }
            }
        }
        dialog?.let { kind ->
            val help = kind == "help"
            InfoDialog(
                if (help) "راهنمای برنامه" else "درباره برنامه و سازنده",
                if (help) "در جستجوی واژگان می‌توانید واژه را به انگلیسی یا معنی فارسی پیدا کنید. در آموزش واژگان، سطح CEFR را انتخاب کنید و واژه‌ها را همراه با معنی، تعریف، مثال و تلفظ یاد بگیرید. با لمس آیکون بلندگو، تلفظ انگلیسی آمریکایی از طریق موتور گفتار گوشی پخش می‌شود. در بخش گرامر، درس‌های سطح‌بندی‌شده را بخوانید و با آزمون کوتاه هر درس تمرین کنید."
                else "American English Vocabulary\n\nبرنامه‌ای برای کمک به فارسی‌زبانان در یادگیری انگلیسی آمریکایی.",
                { dialog = null }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    onSearch: () -> Unit, onVocabulary: () -> Unit, onGrammar: () -> Unit,
    onHelp: () -> Unit, onAbout: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Page), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Navy, Color(0xFF203D68)))).padding(horizontal = 22.dp, vertical = 30.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
                        Text("A", color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("AMERICAN ENGLISH", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text("انگلیسی را کاربردی یاد بگیر", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(9.dp))
                Text("واژگان و مهارت‌های زبان انگلیسی آمریکایی", color = Color(0xFFD7E4F8), fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("LEARN", Gold); Pill("PRACTICE", Color(0xFF78D5C8)); Pill("GROW", Color(0xFFB6C9FF))
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(23.dp))
            Text("از کجا شروع می‌کنی؟", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text("بخش موردنظرت را انتخاب کن", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(19.dp))
            ActionCard("جستجوی واژگان", "واژه موردنظرت را سریع پیدا کن", "⌕", Blue, onSearch)
            Spacer(Modifier.height(12.dp))
            ActionCard("آموزش واژگان", "یادگیری واژه‌ها به‌صورت هدفمند", "✦", Teal, onVocabulary)
            Spacer(Modifier.height(12.dp))
            ActionCard("آموزش گرامر", "قواعد زبان را قدم‌به‌قدم یاد بگیر", "文", Color(0xFF8B5CF6), onGrammar)
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE1E7F0))
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onHelp) { Text("راهنمای برنامه", color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                Text("•", color = Color(0xFFB3BDCC))
                TextButton(onClick = onAbout) { Text("درباره برنامه و سازنده", color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun Pill(label: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(30.dp)).background(Color.White.copy(alpha = .11f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, symbol: String, accent: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().height(82.dp).shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp), color = Color.White, tonalElevation = 1.dp) {
        Row(Modifier.fillMaxSize().padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Text(symbol, color = accent, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Box(Modifier.size(29.dp).clip(CircleShape).background(accent.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
                Text("‹", color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InfoDialog(title: String, body: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(.88f), shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 18.dp) {
            Column(Modifier.padding(22.dp)) {
                Text(title, color = Navy, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(13.dp))
                Text(body, color = Ink, fontSize = 14.sp, lineHeight = 25.sp)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onDismiss, Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Text("متوجه شدم", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
