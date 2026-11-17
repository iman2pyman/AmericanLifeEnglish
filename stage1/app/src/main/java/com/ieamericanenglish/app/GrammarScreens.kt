package com.ieamericanenglish.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class GrammarLesson(
    val level: String,
    val title: String,
    val subtitle: String,
    val explanation: String,
    val structure: String,
    val examples: List<Pair<String, String>>,
    val question: String,
    val prompt: String,
    val choices: List<String>,
    val answer: Int,
    val reason: String
)

private val grammarLessons = listOf(
    GrammarLesson("A1","فعل to be در زمان حال","am / is / are",
        "فعل to be برای معرفی، بیان هویت، شغل، وضعیت و ویژگی‌ها استفاده می‌شود. شکل فعل با فاعل هماهنگ می‌شود.",
        "I am  •  He/She/It is  •  You/We/They are",
        listOf("I am a student." to "من دانش‌آموز / دانشجو هستم.","She is happy." to "او خوشحال است.","They are at home." to "آن‌ها در خانه هستند."),
        "کدام گزینه درست است؟","She ___ my friend.",listOf("am","is","are"),1,"برای فاعل She از is استفاده می‌کنیم."),
    GrammarLesson("A1","حال ساده","Present Simple",
        "برای عادت‌ها، برنامه‌های روزانه و واقعیت‌های عمومی از حال ساده استفاده می‌کنیم. برای he، she و it معمولاً به فعل s یا es اضافه می‌شود.",
        "I/You/We/They + verb  •  He/She/It + verb-s",
        listOf("I work every day." to "من هر روز کار می‌کنم.","He studies English." to "او انگلیسی مطالعه می‌کند.","Do you live here?" to "آیا اینجا زندگی می‌کنی؟"),
        "گزینه درست را انتخاب کن: He ___ coffee every morning.",listOf("drink","drinks","drinking"),1,"در جمله مثبت حال ساده، برای He فعل s می‌گیرد: drinks."),
    GrammarLesson("A1","حروف تعریف a و an","Articles",
        "a و an قبل از اسم مفرد قابل‌شمارش می‌آیند. انتخاب آن‌ها به صدای ابتدای کلمه بستگی دارد، نه فقط حرف اول آن.",
        "a + صدای همخوان  •  an + صدای واکه",
        listOf("a book" to "یک کتاب","an apple" to "یک سیب","an hour" to "یک ساعت (h تلفظ نمی‌شود)"),
        "کدام عبارت درست است؟",listOf("a orange","an orange","the orange (در همه موقعیت‌ها)"),1,"orange با صدای واکه شروع می‌شود، پس an orange درست است."),
    GrammarLesson("A1","ضمیرهای فاعلی","Subject Pronouns",
        "ضمیر فاعلی جای نام شخص یا چیز را در جایگاه فاعل می‌گیرد: I, you, he, she, it, we, they.",
        "Subject + verb",
        listOf("Mary is a nurse. She is kind." to "مری پرستار است. او مهربان است.","The phone is new. It is expensive." to "تلفن جدید است. گران است."),
        "جای خالی را کامل کن: Tom and I are friends. ___ study together.",listOf("We","They","He"),0,"Tom and I یعنی «من و تام»؛ ضمیر مناسب We است."),
    GrammarLesson("A2","گذشته ساده","Past Simple",
        "برای کاری که در زمان مشخصی در گذشته تمام شده است از گذشته ساده استفاده می‌کنیم. افعال باقاعده معمولاً ed می‌گیرند و افعال بی‌قاعده شکل ویژه دارند.",
        "Subject + past verb  •  did + subject + base verb?",
        listOf("I visited my parents yesterday." to "دیروز به دیدن والدینم رفتم.","She went to work." to "او به محل کار رفت.","Did you call me?" to "آیا به من زنگ زدی؟"),
        "گذشته فعل go کدام است؟",listOf("goed","went","gone"),1,"گذشته ساده go برابر went است؛ gone شکل اسم مفعول است."),
    GrammarLesson("A2","آینده با will و going to","Future Forms",
        "will برای تصمیم لحظه‌ای، قول و پیش‌بینی به‌کار می‌رود. going to بیشتر برای برنامه یا قصد قبلی و پیش‌بینی بر اساس نشانه‌ها استفاده می‌شود.",
        "will + base verb  •  am/is/are + going to + base verb",
        listOf("I will help you." to "به تو کمک خواهم کرد.","We are going to move next month." to "ماه آینده قصد داریم اسباب‌کشی کنیم."),
        "برای برنامه‌ای که از قبل تصمیم گرفته‌ایم کدام مناسب‌تر است؟",listOf("going to","am going","will be"),0,"برای قصد و برنامه قبلی معمولاً از going to استفاده می‌کنیم."),
    GrammarLesson("A2","حروف اضافه زمان","Prepositions of Time",
        "معمولاً at برای ساعت دقیق، on برای روزها و تاریخ‌ها و in برای ماه‌ها، سال‌ها و بازه‌های طولانی‌تر استفاده می‌شود.",
        "at + time  •  on + day/date  •  in + month/year",
        listOf("at 7:30" to "ساعت ۷:۳۰","on Monday" to "روز دوشنبه","in November" to "در ماه نوامبر"),
        "کدام گزینه درست است؟ I was born ___ 2001.",listOf("at","on","in"),2,"برای سال از in استفاده می‌کنیم: in 2001."),
    GrammarLesson("B1","حال کامل","Present Perfect",
        "حال کامل برای تجربه‌ها، کارهای انجام‌شده با نتیجه مرتبط با اکنون، یا موقعیتی که از گذشته شروع شده و هنوز ادامه دارد استفاده می‌شود.",
        "have/has + past participle",
        listOf("I have visited New York." to "من از نیویورک دیدن کرده‌ام.","She has lived here for five years." to "او پنج سال است اینجا زندگی می‌کند."),
        "گزینه درست را انتخاب کن: They ___ finished the project.",listOf("has","have","having"),1,"برای فاعل They از have استفاده می‌شود."),
    GrammarLesson("B1","جملات شرطی نوع اول","First Conditional",
        "شرطی نوع اول درباره موقعیت‌های واقعی یا محتمل در آینده صحبت می‌کند. در بخش شرط از حال ساده و در نتیجه معمولاً از will استفاده می‌شود.",
        "If + present simple, will + base verb",
        listOf("If it rains, we will stay home." to "اگر باران ببارد، در خانه می‌مانیم.","If you study, you will improve." to "اگر درس بخوانی، پیشرفت خواهی کرد."),
        "جمله را کامل کن: If I have time, I ___ call you.",listOf("will","would","am"),0,"در نتیجه شرطی نوع اول از will به‌همراه شکل ساده فعل استفاده می‌شود.")
)

@Composable
fun GrammarScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf<GrammarLesson?>(null) }
    var level by remember { mutableStateOf("همه") }
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC)).padding(horizontal = 18.dp, vertical = 12.dp)) {
        if (selected == null) {
            ScreenHeader("آموزش گرامر", onBack)
            Text("درس‌های کوتاه و کاربردی", color = Color(0xFF14213D), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(5.dp))
            Text("از سطح پایه شروع کن و با آزمون هر درس، یادگیری‌ات را بسنج.", color = Color(0xFF718096), fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("همه","A1","A2","B1").forEach { item ->
                    FilterChip(selected = level == item, onClick = { level = item }, label = { Text(item) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                grammarLessons.filter { level == "همه" || it.level == level }.forEach { lesson ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { selected = lesson },
                        shape = RoundedCornerShape(17.dp), color = Color.White, shadowElevation = 2.dp
                    ) {
                        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(lesson.title, color = Color(0xFF18243A), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(lesson.subtitle, color = Color(0xFF718096), fontSize = 12.sp)
                            }
                            Surface(shape = RoundedCornerShape(9.dp), color = Color(0xFF2563EB).copy(alpha = .10f)) {
                                Text(lesson.level, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            ScreenHeader("درس گرامر", onBack = { selected = null })
            GrammarLessonDetail(lesson = selected!!)
        }
    }
}

@Composable
private fun GrammarLessonDetail(lesson: GrammarLesson) {
    var answer by remember(lesson) { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(lesson.title, color = Color(0xFF14213D), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(5.dp))
        Text("سطح ${lesson.level}  •  ${lesson.subtitle}", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        GrammarPanel("توضیح فارسی", lesson.explanation)
        GrammarPanel("ساختار", lesson.structure)
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
            Column(Modifier.padding(15.dp)) {
                Text("مثال‌ها", color = Color(0xFF14213D), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(Modifier.height(9.dp))
                lesson.examples.forEach { (en, fa) ->
                    Text(en, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(fa, color = Color(0xFF718096), fontSize = 12.sp)
                    Spacer(Modifier.height(9.dp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
            Column(Modifier.padding(15.dp)) {
                Text("آزمون کوتاه", color = Color(0xFF14213D), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Text(lesson.question, color = Color(0xFF18243A), fontSize = 13.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(4.dp))
                Text(lesson.prompt, color = Color(0xFF2563EB), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
                Spacer(Modifier.height(8.dp))
                lesson.choices.forEachIndexed { index, choice ->
                    val selectedColor = when {
                        answer == null -> Color(0xFFF4F7FC)
                        index == lesson.answer -> Color(0xFFE2F5ED)
                        answer == index -> Color(0xFFFFE8E8)
                        else -> Color(0xFFF4F7FC)
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { answer = index },
                        shape = RoundedCornerShape(10.dp), color = selectedColor
                    ) {
                        Text(choice, Modifier.padding(12.dp), color = Color(0xFF18243A), fontSize = 13.sp)
                    }
                }
                if (answer != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(if (answer == lesson.answer) "آفرین! پاسخ درست است." else "هنوز درست نیست؛ پاسخ صحیح: ${lesson.choices[lesson.answer]}", color = if (answer == lesson.answer) Color(0xFF0F8B66) else Color(0xFFB54747), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(lesson.reason, color = Color(0xFF718096), fontSize = 12.sp, lineHeight = 20.sp)
                    TextButton(onClick = { answer = null }) { Text("تکرار آزمون") }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GrammarPanel(title: String, body: String) {
    Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = Color(0xFF14213D), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(body, color = Color(0xFF18243A), fontSize = 13.sp, lineHeight = 23.sp, textAlign = TextAlign.Start)
        }
    }
}
