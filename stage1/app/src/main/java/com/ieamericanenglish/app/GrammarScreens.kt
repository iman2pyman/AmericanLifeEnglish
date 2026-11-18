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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class GrammarLesson(
    val level: String,
    val category: String,
    val title: String,
    val explanation: String,
    val structure: String,
    val examples: List<Pair<String, String>>,
    val commonError: String,
    val practice: String,
    val choices: List<String>,
    val answer: Int,
    val reason: String
)

private fun lesson(
    level: String, category: String, title: String, explanation: String, structure: String,
    examples: List<Pair<String, String>>, commonError: String,
    practice: String, choices: List<String>, answer: Int, reason: String
) = GrammarLesson(level, category, title, explanation, structure, examples, commonError, practice, choices, answer, reason)

private val grammarLessons = listOf(
 lesson("A1","جمله‌سازی پایه","ترتیب کلمات در جمله انگلیسی",
  "در جمله خبری ساده انگلیسی معمولاً ابتدا فاعل، سپس فعل و بعد مفعول یا اطلاعات تکمیلی می‌آید. برخلاف فارسی، حذف فاعل در جمله معمولی مجاز نیست. صفت معمولاً پیش از اسم قرار می‌گیرد و قید تکرار غالباً پیش از فعل اصلی و پس از فعل be می‌آید.",
  "Subject + Verb + Object/Complement | I really like this place.",
  listOf("I work in a hospital." to "من در یک بیمارستان کار می‌کنم.","She is very kind." to "او بسیار مهربان است.","We usually eat at home." to "ما معمولاً در خانه غذا می‌خوریم."),
  "نگویید: Works my brother here. درست: My brother works here.",
  "کدام جمله ترتیب درست دارد؟",listOf("Likes she coffee.","She likes coffee.","Coffee she likes is."),1,"در جمله خبری معمولی، فاعل پیش از فعل می‌آید."),
 lesson("A1","جمله‌سازی پایه","فاعل و ضمیرهای فاعلی",
  "فاعل انجام‌دهنده کار یا موضوع جمله است. ضمیرهای فاعلی I, you, he, she, it, we, they جای اسم را در نقش فاعل می‌گیرند. برای اشیا و حیوانات معمولاً it و برای جمع they به‌کار می‌رود.",
  "I / you / he / she / it / we / they + verb",
  listOf("Maria is a teacher. She is friendly." to "ماریا معلم است. او خوش‌برخورد است.","The keys are here. They are on the table." to "کلیدها اینجا هستند. آن‌ها روی میز هستند."),
  "ضمیر مفعولی را به‌جای فاعل نگذارید: Him works here نادرست است.",
  "Tom and I یعنی کدام ضمیر؟",listOf("We","They","He"),0,"Tom and I یعنی من و تام؛ پس ضمیر We است."),
 lesson("A1","فعل‌ها","فعل be در زمان حال و شکل کوتاه",
  "فعل be برای هویت، شغل، سن، مکان، احساس و ویژگی به‌کار می‌رود. شکل آن با فاعل هماهنگ است. در گفتار آمریکایی شکل‌های کوتاه بسیار رایج‌اند: I'm, you're, he's, she's, it's, we're, they're.",
  "I am; he/she/it is; you/we/they are",
  listOf("I'm from Iran." to "من اهل ایران هستم.","She's at work." to "او سر کار است.","They're ready." to "آن‌ها آماده‌اند."),
  "برای I از is استفاده نکنید؛ I am درست است.",
  "گزینه درست: They ___ tired.",listOf("is","am","are"),2,"برای they از are استفاده می‌شود."),
 lesson("A1","فعل‌ها","منفی و پرسشی کردن be",
  "برای منفی کردن be، not را بعد از am/is/are می‌آوریم. برای سؤال بله/خیر، فعل be را پیش از فاعل قرار می‌دهیم. در پاسخ کوتاه، فعل be تکرار می‌شود.",
  "Subject + be + not | Be + subject ...? | Yes, I am. / No, I'm not.",
  listOf("He isn't busy." to "او مشغول نیست.","Are you okay? Yes, I am." to "حالت خوب است؟ بله."),
  "برای سؤال با be از do استفاده نکنید: Do you are ready نادرست است.",
  "کدام سؤال درست است؟",listOf("Are she home?","Is she home?","Does she is home?"),1,"برای she شکل Is به ابتدای سؤال می‌آید."),
 lesson("A1","اسم‌ها","اسم مفرد، جمع و جمع‌های بی‌قاعده",
  "اسم قابل‌شمارش مفرد معمولاً به تعیین‌کننده‌ای مانند a, an, the, my نیاز دارد. جمع بیشتر اسم‌ها با s یا es ساخته می‌شود؛ اما برخی جمع‌های بی‌قاعده‌اند و باید آموخته شوند.",
  "a book → books | a box → boxes | child → children | person → people",
  listOf("I have two children." to "من دو فرزند دارم.","Those boxes are heavy." to "آن جعبه‌ها سنگین‌اند."),
  "پس از عدد بیشتر از یک، اسم قابل‌شمارش معمولاً جمع می‌شود: two car نادرست است.",
  "جمع child چیست؟",listOf("childs","children","childes"),1,"جمع بی‌قاعده child برابر children است."),
 lesson("A1","اسم‌ها","حروف تعریف a, an و the",
  "a و an برای اسم مفرد قابل‌شمارش و غیرمشخص استفاده می‌شوند. انتخاب a یا an به صدای آغازین کلمه بستگی دارد. the برای چیزی به‌کار می‌رود که مشخص یا برای گوینده و شنونده شناخته‌شده است. برای جمع و اسم غیرقابل‌شمارش در معنی کلی معمولاً حرف تعریف نمی‌آید.",
  "a + consonant sound | an + vowel sound | the + specific noun",
  listOf("I need a phone." to "به یک تلفن نیاز دارم.","She ate an apple." to "او یک سیب خورد.","The phone on the desk is mine." to "تلفنی که روی میز است مال من است."),
  "an hour درست است چون h تلفظ نمی‌شود؛ a university درست است چون آغاز آن صدای /y/ دارد.",
  "کدام درست است؟",listOf("an university","a university","the university در همه موقعیت‌ها"),1,"university با صدای /y/ آغاز می‌شود که صدای همخوان‌مانند است."),
 lesson("A1","اسم‌ها","اسم‌های قابل‌شمارش و غیرقابل‌شمارش",
  "اسم قابل‌شمارش را می‌توان شمرد و شکل جمع دارد: chair, idea. اسم غیرقابل‌شمارش معمولاً جمع بسته نمی‌شود: water, information, advice, furniture. برای مقدار آن‌ها از واحدهایی مانند a glass of و a piece of استفاده می‌کنیم.",
  "many/few + countable | much/little + uncountable | a piece of advice",
  listOf("How much water do you need?" to "چقدر آب نیاز داری؟","I have a few questions." to "چند سؤال دارم.","She gave me some advice." to "او مقداری توصیه به من کرد."),
  "advice و information در انگلیسی استاندارد غیرقابل‌شمارش‌اند؛ advices و informations معمولاً نادرست‌اند.",
  "کدام ترکیب درست است؟",listOf("many information","a piece of advice","two advice"),1,"advice غیرقابل‌شمارش است؛ a piece of advice درست است."),
 lesson("A1","ضمیرها","ضمیرهای مفعولی و ملکی",
  "ضمیر مفعولی بعد از فعل یا حرف اضافه می‌آید: me, you, him, her, it, us, them. صفت ملکی پیش از اسم می‌آید: my, your, his, her, its, our, their. ضمیر ملکی به‌تنهایی می‌آید: mine, yours, his, hers, ours, theirs.",
  "verb + object pronoun | possessive adjective + noun | possessive pronoun",
  listOf("Please call me." to "لطفاً به من زنگ بزن.","This is her bag. The bag is hers." to "این کیف اوست. کیف مال اوست."),
  "its صفت ملکی است؛ it's شکل کوتاه it is یا it has است.",
  "جای خالی: This seat is ___. (مال من)",listOf("my","mine","me"),1,"mine ضمیر ملکی مستقل است و بدون اسم می‌آید."),
 lesson("A1","زمان‌ها","حال ساده: کاربرد و ساخت",
  "حال ساده برای عادت‌ها، برنامه‌های ثابت، واقعیت‌ها و وضعیت‌های نسبتاً پایدار به‌کار می‌رود. در سوم‌شخص مفرد (he, she, it) به فعل معمولاً s یا es اضافه می‌شود. قیدهای رایج: always, usually, often, sometimes, never.",
  "I/you/we/they + base verb | he/she/it + verb-s/es",
  listOf("I start work at eight." to "من ساعت هشت کار را شروع می‌کنم.","He watches TV after dinner." to "او بعد از شام تلویزیون تماشا می‌کند."),
  "پس از does در سؤال یا منفی، فعل اصلی بدون s می‌آید.",
  "She ___ English every day.",listOf("study","studies","studying"),1,"در سوم‌شخص مفرد study به studies تبدیل می‌شود."),
 lesson("A1","زمان‌ها","منفی و سؤال در حال ساده",
  "برای بیشتر فعل‌ها از do و does کمک می‌گیریم. در منفی don't/doesn't و در سؤال Do/Does به‌کار می‌رود. فعل اصلی پس از does/doesn't به شکل ساده برمی‌گردد.",
  "Do + I/you/we/they + base verb? | Does + he/she/it + base verb? | don't/doesn't + base verb",
  listOf("Do you drive?" to "آیا رانندگی می‌کنی؟","She doesn't work on Sundays." to "او یکشنبه‌ها کار نمی‌کند."),
  "Does he likes نادرست است؛ Does he like درست است.",
  "کدام جمله درست است؟",listOf("Does she works here?","Does she work here?","Do she work here?"),1,"پس از Does، فعل بدون s می‌آید."),
 lesson("A1","زمان‌ها","حال استمراری",
  "حال استمراری برای کاری که اکنون در جریان است، وضعیت موقت یا برنامه نزدیک از پیش هماهنگ‌شده استفاده می‌شود. فعل be با شکل ing فعل اصلی می‌آید. برخی افعال حالتی مانند know و believe معمولاً در حالت استمراری نمی‌آیند.",
  "am/is/are + verb-ing",
  listOf("I'm studying right now." to "همین الان دارم درس می‌خوانم.","We're meeting Anna tonight." to "امشب آنا را ملاقات می‌کنیم."),
  "برای فعل‌هایی مانند know و need معمولاً حال ساده به‌کار می‌رود: I am knowing نادرست است.",
  "They ___ dinner now.",listOf("have","are having","has"),1,"برای کاری که اکنون در جریان است از are having استفاده می‌کنیم."),
 lesson("A1","زمان‌ها","تفاوت حال ساده و حال استمراری",
  "حال ساده عادت یا واقعیت را بیان می‌کند؛ حال استمراری بر موقتی یا در جریان بودن تأکید دارد. مقایسه: I work downtown (محل کار معمول) و I'm working downtown this month (وضعیت موقت این ماه).",
  "routine/fact: present simple | happening/temporary: present continuous",
  listOf("She usually drives, but today she's taking the bus." to "او معمولاً رانندگی می‌کند، اما امروز با اتوبوس می‌رود."),
  "وجود now همیشه اجباری نیست؛ معنی و بافت جمله زمان را مشخص می‌کند.",
  "برای عادت روزانه کدام مناسب‌تر است؟",listOf("I am walking to work every day.","I walk to work every day.","I walked to work now."),1,"برای عادت از حال ساده استفاده می‌شود."),
 lesson("A1","زمان‌ها","گذشته ساده: افعال باقاعده و بی‌قاعده",
  "گذشته ساده برای رویدادی تمام‌شده در گذشته، معمولاً با زمان مشخص، به‌کار می‌رود. افعال باقاعده ed می‌گیرند؛ افعال بی‌قاعده شکل جداگانه دارند. قیدهای رایج yesterday, last week, in 2020 هستند.",
  "Subject + past form | regular: work → worked | go → went",
  listOf("We moved here last year." to "ما پارسال به اینجا نقل مکان کردیم.","He bought a new car." to "او یک ماشین جدید خرید."),
  "بعد از did فعل ساده می‌آید، نه شکل گذشته: Did you went نادرست است.",
  "گذشته buy چیست؟",listOf("buyed","bought","brought"),1,"گذشته buy برابر bought است."),
 lesson("A1","زمان‌ها","منفی و سؤال در گذشته ساده",
  "برای منفی کردن بیشتر فعل‌ها از didn't و برای سؤال از did استفاده می‌کنیم. پس از آن‌ها فعل اصلی به شکل پایه می‌آید. فعل be در گذشته (was/were) بدون did سؤال و منفی می‌شود.",
  "didn't + base verb | Did + subject + base verb? | was/were + not",
  listOf("I didn't see him." to "من او را ندیدم.","Did they call you?" to "آیا آن‌ها به تو زنگ زدند؟"),
  "I didn't went نادرست است؛ I didn't go درست است.",
  "کدام جمله صحیح است؟",listOf("Did you ate?","Did you eat?","Do you ate?"),1,"پس از did از شکل پایه eat استفاده می‌کنیم."),
 lesson("A1","زمان‌ها","گذشته استمراری",
  "گذشته استمراری کاری را نشان می‌دهد که در زمان مشخصی در گذشته در جریان بوده است. همچنین برای پس‌زمینه یک داستان یا کاری که رویداد دیگری آن را قطع کرده استفاده می‌شود.",
  "was/were + verb-ing | while + past continuous; when + past simple",
  listOf("I was driving when it started to rain." to "وقتی باران شروع شد، داشتم رانندگی می‌کردم.","They were sleeping at midnight." to "آن‌ها نیمه‌شب خواب بودند."),
  "برای فاعل جمع they از were استفاده می‌شود، نه was.",
  "I ___ when you called.",listOf("slept","was sleeping","am sleeping"),1,"کار در جریان در گذشته با was sleeping بیان می‌شود."),
 lesson("A1","زمان‌ها","آینده با will",
  "will برای تصمیم لحظه‌ای، قول، پیشنهاد، درخواست و پیش‌بینی استفاده می‌شود. بعد از will همیشه شکل پایه فعل می‌آید و برای همه فاعل‌ها یکسان است. شکل منفی won't است.",
  "will + base verb | won't + base verb | Will + subject + base verb?",
  listOf("I'll help you with that." to "در آن مورد به تو کمک می‌کنم.","I think it will rain." to "فکر می‌کنم باران ببارد."),
  "بعد از will از to یا s سوم‌شخص استفاده نمی‌کنیم: will goes نادرست است.",
  "کدام درست است؟",listOf("She will calls.","She will call.","She wills call."),1,"پس از will شکل ساده call می‌آید."),
 lesson("A1","زمان‌ها","آینده با be going to",
  "ساختار be going to برای قصد و برنامه‌ای که پیش‌تر شکل گرفته یا پیش‌بینی بر اساس شواهد فعلی به‌کار می‌رود. فعل be باید با فاعل هماهنگ شود.",
  "am/is/are + going to + base verb",
  listOf("We're going to move in June." to "قرار است در ژوئن نقل مکان کنیم.","Look at those clouds. It's going to rain." to "به آن ابرها نگاه کن؛ به‌زودی باران می‌بارد."),
  "پس از going to فعل پایه می‌آید؛ going to studying نادرست است.",
  "برای برنامه قبلی کدام طبیعی‌تر است؟",listOf("I am going study.","I'm going to study.","I going to study."),1,"ساختار کامل am going to + فعل پایه است."),
 lesson("A1","زمان‌ها","قیدهای تکرار و جایگاه آن‌ها",
  "قیدهای always, usually, often, sometimes, rarely و never میزان تکرار را نشان می‌دهند. معمولاً پیش از فعل اصلی و پس از فعل be قرار می‌گیرند. عبارت‌هایی مانند every day و once a week معمولاً انتهای جمله می‌آیند.",
  "before main verb | after be | end-position time phrase",
  listOf("I usually take the bus." to "من معمولاً با اتوبوس می‌روم.","She's always on time." to "او همیشه به‌موقع است."),
  "She always is late از نظر جایگاه معمول خنثی نیست؛ She is always late طبیعی‌تر است.",
  "جایگاه طبیعی قید: He ___ late.",listOf("is usually","usually is (در حالت خنثی)","usually late is"),0,"قید پس از فعل be قرار می‌گیرد."),
 lesson("A1","سؤال‌سازی","سؤال‌های Wh-",
  "کلمات پرسشی اطلاعات خاص می‌خواهند: what (چه)، where (کجا)، when (چه‌وقت)، who (چه‌کسی)، why (چرا)، which (کدام)، whose (مال چه‌کسی)، how (چطور). در بیشتر زمان‌ها از فعل کمکی استفاده می‌شود؛ اگر خود کلمه پرسشی فاعل باشد، ترتیب متفاوت است.",
  "Wh-word + auxiliary + subject + main verb?",
  listOf("Where do you live?" to "کجا زندگی می‌کنی؟","Why did she leave?" to "چرا او رفت؟","Who called you?" to "چه کسی به تو زنگ زد؟"),
  "در Who called you? چون who فاعل است، do/did اضافه نمی‌کنیم.",
  "برای پرسیدن مکان کدام واژه است؟",listOf("When","Where","Why"),1,"Where برای پرسیدن مکان است."),
 lesson("A1","سؤال‌سازی","سؤال‌های بله/خیر و پاسخ کوتاه",
  "سؤال بله/خیر با فعل کمکی یا modal آغاز می‌شود. پاسخ کوتاه معمولاً همان فعل کمکی را با ضمیر تکرار می‌کند؛ پاسخ‌هایی مانند Yes, I do و No, she isn't طبیعی‌اند.",
  "Auxiliary + subject + verb? | Yes, subject + auxiliary. / No, subject + auxiliary + not.",
  listOf("Do you work here? Yes, I do." to "اینجا کار می‌کنی؟ بله.","Is he ready? No, he isn't." to "او آماده است؟ نه."),
  "در پاسخ کوتاه، فعل اصلی را بی‌دلیل تکرار نکنید: Yes, I work معمولاً پاسخ کوتاه استاندارد این سؤال نیست.",
  "پاسخ کوتاه Do you drive? کدام است؟",listOf("Yes, I am.","Yes, I do.","Yes, I drive."),1,"سؤال با do ساخته شده و پاسخ کوتاه نیز do دارد."),
 lesson("A1","صفت و قید","صفت‌ها و جایگاه آن‌ها",
  "صفت ویژگی اسم را بیان می‌کند و معمولاً پیش از اسم یا پس از افعال ربطی مانند be, seem, feel قرار می‌گیرد. صفت در انگلیسی با جمع یا جنسیت تغییر نمی‌کند.",
  "adjective + noun | be/seem/feel + adjective",
  listOf("a comfortable chair" to "یک صندلی راحت","The room feels cold." to "اتاق سرد به نظر می‌رسد."),
  "پس از look/feel/sound در معنی ربطی صفت می‌آید: sounds good نه sounds well (در معنی کیفیت صدا).",
  "کدام درست است؟",listOf("a car expensive","an expensive car","an expensively car"),1,"صفت پیش از اسم می‌آید."),
 lesson("A1","صفت و قید","قیدهای حالت",
  "قید حالت چگونگی انجام فعل را توضیح می‌دهد و بسیاری از آن‌ها با افزودن ly به صفت ساخته می‌شوند: quick/quickly. اما استثناهایی مانند fast و hard وجود دارند. good صفت و well معمولاً قید است.",
  "verb + adverb | adjective + -ly (common pattern)",
  listOf("She speaks clearly." to "او واضح صحبت می‌کند.","He drives fast." to "او سریع رانندگی می‌کند.","You did well." to "خوب انجام دادی."),
  "hard به معنی با تلاش و hardly به معنی به‌ندرت/تقریباً نه است؛ این دو هم‌معنی نیستند.",
  "قید مناسب برای speak چیست؟",listOf("clear","clearly","clearness"),1,"برای توصیف چگونگی صحبت کردن، clearly قید است."),
 lesson("A1","مقدار و اشاره","this, that, these, those",
  "this و these برای نزدیک و that و those برای دور به‌کار می‌روند. this/that با اسم مفرد و these/those با اسم جمع می‌آیند و می‌توانند پیش از اسم یا مستقل استفاده شوند.",
  "this/that + singular | these/those + plural",
  listOf("This is my phone." to "این تلفن من است.","Those shoes are expensive." to "آن کفش‌ها گران‌اند."),
  "These book نادرست است؛ these books درست است.",
  "برای چند شیء نزدیک کدام را به‌کار می‌بریم؟",listOf("this","these","that"),1,"these برای اسم جمع نزدیک استفاده می‌شود."),
 lesson("A1","مقدار و اشاره","some, any, no و ترکیب‌های آن‌ها",
  "some بیشتر در جمله مثبت و پیشنهاد یا درخواست مؤدبانه می‌آید. any در سؤال و منفی رایج است. no پیش از اسم می‌آید و معنی هیچ می‌دهد. ترکیب‌ها: someone, anything, nowhere و مانند آن.",
  "some + noun | any in questions/negatives | no + noun",
  listOf("I have some time." to "کمی وقت دارم.","Do you have any questions?" to "سؤالی داری؟","There is no milk." to "هیچ شیری نیست."),
  "پس از no اسم بدون not می‌آید؛ no don't have نادرست است.",
  "در سؤال معمولی کدام رایج‌تر است؟",listOf("some questions","any questions","no questions"),1,"در سؤال خنثی معمولاً any به‌کار می‌رود."),
 lesson("A1","مقدار و اشاره","there is و there are",
  "برای گفتن وجود داشتن چیزی از there is برای مفرد و غیرقابل‌شمارش و there are برای جمع استفاده می‌کنیم. در گذشته there was/there were و در آینده there will be به‌کار می‌رود.",
  "There is + singular/uncountable | There are + plural",
  listOf("There's a pharmacy nearby." to "یک داروخانه در نزدیکی هست.","There are two bedrooms." to "دو اتاق‌خواب وجود دارد."),
  "با اسم جمع از there is در نوشتار معیار استفاده نکنید.",
  "There ___ three people outside.",listOf("is","are","am"),1,"three people جمع است و are می‌خواهد."),
 lesson("A1","حروف اضافه","حروف اضافه مکان",
  "in برای داخل فضا یا محدوده، on برای سطح، و at برای نقطه یا مکان به‌عنوان موقعیت استفاده می‌شود. کاربردهای واقعی گاهی اصطلاحی‌اند و باید همراه عبارت آموخته شوند.",
  "in a room/city | on a surface/street | at a point/place",
  listOf("She's in the kitchen." to "او داخل آشپزخانه است.","The keys are on the table." to "کلیدها روی میز هستند.","I'll meet you at the entrance." to "دم ورودی می‌بینمت."),
  "برای آدرس و نام خیابان، کاربرد in/on/at به نوع نشانی بستگی دارد؛ الگوها را در عبارت کامل یاد بگیرید.",
  "کتاب روی میز است: The book is ___ the table.",listOf("in","on","at"),1,"برای قرار گرفتن روی سطح از on استفاده می‌کنیم."),
 lesson("A1","حروف اضافه","حروف اضافه زمان",
  "at برای ساعت و زمان دقیق، on برای روز و تاریخ، و in برای ماه، سال، فصل و بازه‌های طولانی به‌کار می‌رود. عبارت‌های ثابت مانند at night و in the morning را نیز باید حفظ کرد.",
  "at 6:00 | on Monday / July 4 | in June / 2025 / winter",
  listOf("The meeting is at noon." to "جلسه ظهر است.","I was born in May." to "من در ماه مه به دنیا آمدم.","We work on Fridays." to "ما جمعه‌ها کار می‌کنیم."),
  "می‌گوییم at night اما in the morning/afternoon/evening.",
  "___ Monday",listOf("in","on","at"),1,"برای روزهای هفته از on استفاده می‌شود."),
 lesson("A2","زمان‌ها","حال کامل: ساخت و کاربرد",
  "حال کامل با have/has و اسم مفعول ساخته می‌شود. برای تجربه تا زمان حال، نتیجه فعلی یک رویداد گذشته و موقعیتی که از گذشته شروع شده و هنوز ادامه دارد به‌کار می‌رود. زمان دقیق و تمام‌شده گذشته معمولاً با گذشته ساده می‌آید.",
  "have/has + past participle",
  listOf("I've visited Chicago twice." to "دو بار از شیکاگو دیدن کرده‌ام.","She's lost her keys." to "او کلیدهایش را گم کرده است (و اکنون آن‌ها را ندارد)."),
  "I have seen him yesterday نادرست است؛ yesterday زمان تمام‌شده است و گذشته ساده می‌خواهد.",
  "She ___ finished the report.",listOf("have","has","having"),1,"برای she از has استفاده می‌شود."),
 lesson("A2","زمان‌ها","حال کامل با for و since",
  "for مدت زمان را نشان می‌دهد (for three years) و since نقطه شروع را (since 2022). با فعل‌های حالتی و موقعیت‌های ادامه‌دار، حال کامل ساده رایج است. برای فعالیتی که بر استمرار آن تأکید داریم، حال کامل استمراری نیز ممکن است.",
  "have/has + past participle + for/since",
  listOf("I've lived here for five years." to "پنج سال است اینجا زندگی می‌کنم.","She's worked here since 2021." to "او از سال ۲۰۲۱ اینجا کار می‌کند."),
  "for Monday نادرست است؛ Monday نقطه شروع است و since Monday می‌گوییم.",
  "برای مدت سه ماه کدام درست است؟",listOf("since three months","for three months","during three months (در این ساخت)"),1,"for با مدت زمان می‌آید."),
 lesson("A2","زمان‌ها","حال کامل استمراری",
  "حال کامل استمراری بر مدت یا روند فعالیتی که در گذشته آغاز شده و تا اکنون ادامه داشته یا تازه متوقف شده تأکید می‌کند. با have/has been و فعل ing ساخته می‌شود.",
  "have/has been + verb-ing",
  listOf("I've been waiting for 20 minutes." to "۲۰ دقیقه است منتظرم.","It's been raining all morning." to "تمام صبح باران باریده/می‌بارد."),
  "برای افعال حالتی مانند know معمولاً حال کامل ساده مناسب‌تر است: I've known her for years.",
  "He ___ working since noon.",listOf("has been","have been","is been"),0,"فاعل he با has been می‌آید."),
 lesson("A2","زمان‌ها","گذشته کامل",
  "گذشته کامل نشان می‌دهد یک عمل پیش از زمان یا رویداد دیگری در گذشته رخ داده است. وقتی ترتیب رویدادها از بافت روشن باشد، استفاده از آن همیشه ضروری نیست.",
  "had + past participle",
  listOf("The train had left before we arrived." to "قطار پیش از رسیدن ما حرکت کرده بود.","I'd never seen snow before that trip." to "پیش از آن سفر هرگز برف ندیده بودم."),
  "برای رویداد ساده و تنها در گذشته، بی‌دلیل از had استفاده نکنید.",
  "By the time I arrived, she ___ .",listOf("left","had left","has left"),1,"حرکت او پیش از رسیدن من اتفاق افتاده بود؛ گذشته کامل."),
 lesson("A2","زمان‌ها","آینده استمراری و آینده کامل",
  "آینده استمراری کاری را که در زمان مشخصی در آینده در جریان خواهد بود بیان می‌کند. آینده کامل کاری را نشان می‌دهد که تا زمان معینی در آینده کامل شده است.",
  "will be + verb-ing | will have + past participle",
  listOf("This time tomorrow, I'll be flying." to "فردا همین موقع در حال پرواز خواهم بود.","By Friday, we'll have finished." to "تا جمعه کار را تمام کرده‌ایم."),
  "پس از will be در آینده استمراری فعل ing و پس از will have اسم مفعول می‌آید.",
  "By 5 p.m., I ___ the work.",listOf("will finish","will have finished","will be finish"),1,"By زمان مشخص آینده با کامل شدن کار، آینده کامل را می‌طلبد."),
 lesson("A2","زمان‌ها","تفاوت زمان‌های اصلی و انتخاب زمان مناسب",
  "انتخاب زمان را بر اساس معنی انجام دهید: عادت (حال ساده)، اکنون/موقت (حال استمراری)، گذشته تمام‌شده (گذشته ساده)، ارتباط گذشته با اکنون (حال کامل)، عمل پیش از گذشته دیگر (گذشته کامل)، قصد قبلی (going to) و تصمیم/قول (will).",
  "Choose tense by time reference, completion, duration and speaker's meaning.",
  listOf("I work nights." to "شب‌کارم (عادت).","I'm working tonight." to "امشب کار می‌کنم (برنامه/وضعیت موقت).","I've worked here for years." to "سال‌هاست اینجا کار می‌کنم."),
  "صرفاً با دیدن یک قید، زمان را انتخاب نکنید؛ کل منظور جمله را بسنجید.",
  "برای عادت کدام درست است؟",listOf("I am going to work every day.","I work every day.","I have worked now every day."),1,"حال ساده برای عادت معمول مناسب است."),
 lesson("A2","افعال کمکی","افعال modal: can, could, may, might",
  "افعال وجهی توانایی، اجازه، احتمال و درخواست را بیان می‌کنند. پس از modal شکل پایه فعل بدون to می‌آید. can توانایی/اجازه، could توانایی گذشته یا درخواست مؤدبانه، may اجازه رسمی یا احتمال و might احتمال ضعیف‌تر را نشان می‌دهد.",
  "modal + base verb | can/could/may/might + verb",
  listOf("Can you help me?" to "می‌توانی کمکم کنی؟","Could I ask a question?" to "می‌توانم سؤالی بپرسم؟","It might snow." to "شاید برف ببارد."),
  "بعد از can/could/might به فعل s یا to اضافه نکنید.",
  "کدام درست است؟",listOf("She can drives.","She can drive.","She cans drive."),1,"بعد از can شکل پایه drive می‌آید."),
 lesson("A2","افعال کمکی","must, have to و should",
  "must برای الزام قوی یا نتیجه‌گیری منطقی، have to برای ضرورت و الزام ناشی از شرایط یا مقررات، و should برای توصیه به‌کار می‌روند. تفاوت mustn't (ممنوع است) با don't have to (ضرورتی ندارد) بسیار مهم است.",
  "must + base | have to + base | should + base",
  listOf("You must wear a seat belt." to "باید کمربند ببندی.","You don't have to come early." to "لازم نیست زود بیایی.","You shouldn't skip meals." to "نباید وعده‌های غذایی را حذف کنی."),
  "mustn't به معنی لازم نیست نیست؛ معنی آن ممنوعیت است.",
  "«لازم نیست» کدام است؟",listOf("mustn't","don't have to","shouldn't"),1,"don't have to نبودِ ضرورت را بیان می‌کند."),
 lesson("A2","افعال کمکی","افعال وجهی گذشته",
  "برای بیان توانایی، احتمال، انتقاد یا الزام در گذشته از ساختارهایی مانند could, had to, should have و might have استفاده می‌کنیم. should have + اسم مفعول برای کاری است که بهتر بود انجام می‌شد اما نشد.",
  "could + base | had to + base | should have + past participle",
  listOf("I had to work late." to "مجبور شدم تا دیر وقت کار کنم.","You should have called me." to "بهتر بود به من زنگ می‌زدی."),
  "بعد از should have شکل پایه نمی‌آید؛ اسم مفعول لازم است.",
  "You ___ told me earlier.",listOf("should have","should","should had"),0,"ساختار درست should have + past participle است."),
 lesson("A2","مقایسه","مقایسه‌ای و عالی",
  "برای صفت کوتاه معمولاً er و est و برای بسیاری از صفت‌های بلند more و most می‌آید. شکل‌های بی‌قاعده مانند good/better/best و bad/worse/worst باید حفظ شوند. than پس از مقایسه‌ای رایج است.",
  "short adj + -er + than | the + -est | more/most + adjective",
  listOf("This route is shorter." to "این مسیر کوتاه‌تر است.","She's more experienced than me." to "او از من باتجربه‌تر است.","That's the best option." to "آن بهترین گزینه است."),
  "more easier نادرست است؛ easier کافی است.",
  "صفت عالی good چیست؟",listOf("goodest","better","best"),2,"شکل عالی good برابر best است."),
 lesson("A2","مقایسه","as...as و مقایسه برابر و نابرابر",
  "ساختار as + صفت + as برای برابری و not as...as برای نابرابری به‌کار می‌رود. less...than نیز معنی کمتر از را می‌دهد.",
  "as + adjective + as | not as + adjective + as | less + adjective + than",
  listOf("This phone is as good as mine." to "این تلفن به خوبی تلفن من است.","It's not as expensive as I expected." to "به گرانی‌ای که انتظار داشتم نیست."),
  "در این ساختار از صفت پایه استفاده می‌شود، نه شکل مقایسه‌ای.",
  "This bag is ___ heavy as that one.",listOf("as","more","than"),0,"ساختار برابری as...as است."),
 lesson("A2","مقدار","much, many, a lot of, few و little",
  "many با اسم جمع قابل‌شمارش و much با اسم غیرقابل‌شمارش می‌آید. a lot of با هر دو نوع طبیعی است. few/a few برای اسم جمع و little/a little برای غیرقابل‌شمارش‌اند؛ a- مفهوم مقداری/تعدادی مثبت‌تر می‌دهد.",
  "many/few + plural countable | much/little + uncountable | a lot of + both",
  listOf("We don't have much time." to "وقت زیادی نداریم.","A few people called." to "چند نفر زنگ زدند.","There's little information available." to "اطلاعات کمی در دسترس است."),
  "few و a few از نظر معنی یکسان نیستند: few تقریباً هیچ، a few چندتا.",
  "___ apples are left.",listOf("Much","A few","A little"),1,"apples قابل‌شمارش و جمع است؛ a few درست است."),
 lesson("A2","مصدر و ing","مصدر با to و کاربرد آن",
  "to-infinitive پس از بسیاری از افعال مانند want, need, decide, hope و پس از صفت‌ها می‌آید. همچنین برای بیان هدف استفاده می‌شود. بعضی افعال الگوی خاص خود را دارند و باید همراه مثال آموخته شوند.",
  "verb + to + base verb | adjective + to-infinitive | to show purpose",
  listOf("I want to learn English." to "می‌خواهم انگلیسی یاد بگیرم.","She came here to work." to "او برای کار به اینجا آمد."),
  "پس از want از فعل ing استفاده نکنید در الگوی معمول: want learning نادرست است.",
  "I decided ___ a new job.",listOf("to find","finding","find"),0,"پس از decide، مصدر با to می‌آید."),
 lesson("A2","مصدر و ing","gerund: فعل ing به‌عنوان اسم",
  "شکل ing می‌تواند نقش اسم داشته باشد و پس از افعالی مانند enjoy, avoid, finish, suggest و حروف اضافه می‌آید. بعد از حرف اضافه همیشه شکل ing فعل به‌کار می‌رود.",
  "enjoy/avoid/finish + verb-ing | preposition + verb-ing",
  listOf("I enjoy cooking." to "از آشپزی لذت می‌برم.","She's interested in learning." to "او به یادگیری علاقه‌مند است.","He left without saying goodbye." to "او بدون خداحافظی رفت."),
  "پس از حرف اضافه to در look forward to نیز ing می‌آید: look forward to seeing you.",
  "She avoids ___ late.",listOf("to arrive","arriving","arrive"),1,"پس از avoid شکل ing می‌آید."),
 lesson("A2","مصدر و ing","افعالی که با to یا ing معنی متفاوت دارند",
  "برخی افعال با تغییر ساختار، معنی متفاوت پیدا می‌کنند. stop doing یعنی انجام کاری را متوقف کردن؛ stop to do یعنی توقف کردن برای انجام کاری. remember/forget doing به خاطره عمل و remember/forget to do به یادآوری انجام آن مربوط است.",
  "stop doing ≠ stop to do | remember doing ≠ remember to do",
  listOf("I stopped smoking." to "سیگار کشیدن را ترک کردم.","I stopped to answer the phone." to "ایستادم تا به تلفن جواب بدهم."),
  "این افعال را با یک قاعده واحد حفظ نکنید؛ تفاوت معنی را همراه مثال یاد بگیرید.",
  "I stopped ___ a coffee. (ایستادم تا قهوه بگیرم)",listOf("drinking","to get","get"),1,"هدف از توقف، گرفتن قهوه است؛ stop to get."),
 lesson("A2","مجهول","مجهول: حال و گذشته",
  "وقتی انجام‌دهنده کار مهم نیست یا معلوم نیست، یا نتیجه و دریافت‌کننده عمل مهم‌تر است، از مجهول استفاده می‌کنیم. زمان فعل be تغییر می‌کند و فعل اصلی به اسم مفعول می‌رود. عامل با by می‌تواند ذکر شود.",
  "be (in the required tense) + past participle",
  listOf("English is spoken here." to "اینجا انگلیسی صحبت می‌شود.","The package was delivered yesterday." to "بسته دیروز تحویل داده شد."),
  "مجهول بدون شکل درست be ناقص است؛ The car repaired نادرست است.",
  "The emails ___ yesterday.",listOf("were sent","sent","are send"),0,"گذشته مجهول: were + اسم مفعول sent."),
 lesson("A2","مجهول","مجهول در زمان‌های دیگر و با modal",
  "مجهول در زمان‌های مختلف با تغییر be ساخته می‌شود: is being repaired (حال استمراری)، has been completed (حال کامل)، will be delivered (آینده)، must be done (modal). اسم مفعول ثابت می‌ماند.",
  "modal + be + past participle | has/have been + past participle",
  listOf("Your order will be shipped tomorrow." to "سفارش شما فردا ارسال خواهد شد.","The road is being repaired." to "جاده در حال تعمیر است."),
  "بعد از modal در مجهول be می‌آید، نه been مگر ساختار کامل اقتضا کند.",
  "The work must ___ today.",listOf("finish","be finished","be finish"),1,"مجهول با modal: must be + past participle."),
 lesson("A2","شرطی‌ها","شرطی صفر و اول",
  "شرطی صفر برای واقعیت‌ها و نتایج همیشگی است و معمولاً هر دو بخش حال ساده‌اند. شرطی اول برای امکان واقعی آینده است؛ در بخش if حال ساده و در نتیجه معمولاً will می‌آید.",
  "Zero: If + present, present | First: If + present, will + base",
  listOf("If you heat ice, it melts." to "اگر یخ را گرم کنی، آب می‌شود.","If it rains, we'll stay home." to "اگر باران ببارد، خانه می‌مانیم."),
  "در بخش if شرطی اول معمولاً will نمی‌آید.",
  "If she calls, I ___ answer.",listOf("will","would","am"),0,"شرطی اول در نتیجه از will استفاده می‌کند."),
 lesson("B1","شرطی‌ها","شرطی دوم",
  "شرطی دوم برای موقعیت فرضی، بعید یا غیرواقعی در زمان حال یا آینده است. در بخش شرط گذشته ساده و در نتیجه would + شکل پایه می‌آید. برای همه فاعل‌ها were در حالت رسمی/فرضی رایج است.",
  "If + past simple, would + base verb",
  listOf("If I had more time, I'd travel." to "اگر وقت بیشتری داشتم، سفر می‌کردم.","If I were you, I'd ask." to "اگر جای تو بودم، می‌پرسیدم."),
  "در شرط فرضی از would در هر دو بخش استفاده نکنید.",
  "If he ___ rich, he'd buy a house.",listOf("is","were","will be"),1,"شرطی دوم برای حالت فرضی از were استفاده می‌کند."),
 lesson("B1","شرطی‌ها","شرطی سوم",
  "شرطی سوم درباره گذشته‌ای فرضی است که اتفاق نیفتاده و نتیجه خیالی آن صحبت می‌شود. در بخش if از گذشته کامل و در نتیجه از would have + اسم مفعول استفاده می‌کنیم.",
  "If + past perfect, would have + past participle",
  listOf("If we'd left earlier, we would've caught the train." to "اگر زودتر راه افتاده بودیم، به قطار می‌رسیدیم."),
  "would have را در بخش if قرار ندهید؛ If we would have left نادرست است.",
  "If I had known, I ___ helped.",listOf("would have","will have","would"),0,"نتیجه فرضی گذشته با would have + helped ساخته می‌شود."),
 lesson("B1","شرطی‌ها","شرطی ترکیبی",
  "شرطی ترکیبی گذشته و حال را به هم مرتبط می‌کند؛ مثلاً تصمیم گذشته بر وضعیت فعلی اثر دارد. شکل رایج: if + past perfect و would + فعل پایه.",
  "If + past perfect, would + base verb (past condition → present result)",
  listOf("If I had studied medicine, I'd be a doctor now." to "اگر پزشکی خوانده بودم، الان پزشک بودم."),
  "زمان دو بخش را بر اساس معنی انتخاب کنید؛ هر دو الزاماً هم‌زمان نیستند.",
  "If she had taken the job, she ___ in New York now.",listOf("would live","will live","would have lived"),0,"شرط گذشته، نتیجه فرضی در زمان حال دارد."),
 lesson("B1","گزارش گفتار","گفتار غیرمستقیم: جمله خبری",
  "برای گزارش گفته دیگران، فعل گزارشی مانند said یا told به‌کار می‌رود. اگر فعل گزارشی گذشته باشد، زمان جمله معمولاً یک مرحله به عقب می‌رود؛ اما اگر واقعیت هنوز درست باشد، تغییر زمان همیشه ضروری نیست. ضمیرها و عبارت‌های زمان/مکان نیز با بافت هماهنگ می‌شوند.",
  "said (that) + clause | told + person + (that) + clause",
  listOf("She said (that) she was tired." to "او گفت که خسته است/بود.","He told me he needed help." to "او به من گفت که به کمک نیاز دارد."),
  "say معمولاً مفعول شخص مستقیم نمی‌گیرد: said me نادرست؛ told me یا said to me درست است.",
  "He ___ me that he was busy.",listOf("said","told","spoke"),1,"told با مفعول شخص می‌آید: told me."),
 lesson("B1","گزارش گفتار","گزارش سؤال و دستور",
  "در سؤال غیرمستقیم ترتیب جمله خبری است و do/does/did حذف می‌شود. برای سؤال بله/خیر از if یا whether استفاده می‌کنیم. دستور با told/asked + object + to-infinitive گزارش می‌شود.",
  "asked + wh-word + subject + verb | asked if/whether | told + object + to verb",
  listOf("She asked where I lived." to "او پرسید کجا زندگی می‌کنم.","He asked if I was ready." to "او پرسید آیا آماده‌ام.","They told us to wait." to "آن‌ها به ما گفتند منتظر بمانیم."),
  "Where did I live? را در سؤال غیرمستقیم به where I lived تبدیل کنید.",
  "She asked me where ___ .",listOf("did I work","I worked","do I work"),1,"ترتیب سؤال غیرمستقیم مانند جمله خبری است."),
 lesson("B1","جملات موصولی","جملات موصولی defining و ضمایر موصولی",
  "جمله موصولی اسم را مشخص می‌کند. who برای انسان، which برای چیز، that برای انسان یا چیز در جمله محدودکننده، و whose برای مالکیت استفاده می‌شود. در جمله defining معمولاً ویرگول نمی‌آید.",
  "person: who/that | thing: which/that | possession: whose",
  listOf("The woman who called is my manager." to "زنی که زنگ زد مدیر من است.","The phone that I bought is new." to "تلفنی که خریدم جدید است."),
  "در جمله defining معمولاً that جایگزین who/which می‌شود؛ اما پس از ویرگول در non-defining نمی‌آید.",
  "The man ___ lives next door is a doctor.",listOf("who","where","when"),0,"برای شخص از who استفاده می‌شود."),
 lesson("B1","جملات موصولی","جملات موصولی non-defining",
  "جمله موصولی غیرمحدودکننده اطلاعات اضافی می‌دهد و با ویرگول جدا می‌شود. در این نوع، that استفاده نمی‌شود و ضمیر موصولی مفعولی را معمولاً حذف نمی‌کنیم.",
  "Noun, who/which/whose + clause, ...",
  listOf("My brother, who lives in Texas, is visiting." to "برادرم که در تگزاس زندگی می‌کند، به دیدنم می‌آید."),
  "برای اطلاعات اضافی درباره اسم خاص معمولاً ویرگول لازم است و that مناسب نیست.",
  "My car, ___ I bought last year, is reliable.",listOf("that","which","what"),1,"در جمله موصولی غیرمحدودکننده از which استفاده می‌شود."),
 lesson("B1","حروف اضافه","حروف اضافه پس از صفت و فعل",
  "بسیاری از صفت‌ها و فعل‌ها با حرف اضافه مشخصی می‌آیند و بهتر است به‌صورت ترکیب یاد گرفته شوند: interested in, good at, responsible for, depend on, listen to, wait for, apply for.",
  "adjective/verb + fixed preposition + noun/gerund",
  listOf("She's good at solving problems." to "او در حل مشکلات خوب است.","It depends on the weather." to "به آب‌وهوا بستگی دارد.","I'm looking for a job." to "دنبال کار می‌گردم."),
  "حرف اضافه را از فارسی ترجمه نکنید؛ ترکیب کامل را یاد بگیرید.",
  "I'm interested ___ history.",listOf("on","in","at"),1,"ترکیب درست interested in است."),
 lesson("B1","افعال عبارتی","افعال عبارتی رایج و جداشدنی",
  "phrasal verb از فعل و particle تشکیل می‌شود و معنی آن گاهی از اجزا قابل حدس نیست. در افعال جداشدنی با مفعول اسمی هر دو جایگاه ممکن است؛ اما ضمیر مفعولی معمولاً بین فعل و particle می‌آید: turn it off.",
  "turn off the light / turn the light off / turn it off",
  listOf("Please fill out this form." to "لطفاً این فرم را پر کن.","Turn the TV off." to "تلویزیون را خاموش کن.","Pick me up at six." to "ساعت شش دنبالم بیا."),
  "نگویید turn off it؛ ضمیر باید بین فعل و particle قرار گیرد.",
  "کدام طبیعی است؟",listOf("Turn off it.","Turn it off.","Turn it the off."),1,"در فعل عبارتی جداشدنی، ضمیر بین دو بخش می‌آید."),
 lesson("B1","مجهول و سببی","ساختار have/get something done",
  "از have/get something done وقتی استفاده می‌کنیم که شخص دیگری کاری را برای ما انجام می‌دهد. تمرکز روی خدمت یا نتیجه است، نه انجام‌دهنده.",
  "have/get + object + past participle",
  listOf("I had my car repaired." to "ماشینم را برای تعمیر دادم.","She's getting her hair cut." to "او دارد موهایش را کوتاه می‌کند (توسط آرایشگر)."),
  "این ساختار با have something و انجام مستقیم آن توسط خود شخص فرق دارد.",
  "We ___ the house painted last week.",listOf("had","did","made"),0,"ساختار سببی گذشته با had + object + اسم مفعول است."),
 lesson("B1","جمله‌سازی","جملات امری و درخواست مؤدبانه",
  "جمله امری با شکل پایه فعل شروع می‌شود و فاعل you معمولاً حذف می‌شود. برای منفی کردن don't پیش از فعل می‌آید. برای درخواست مؤدبانه از please, could you و would you mind استفاده می‌شود.",
  "base verb ... | Don't + base verb | Could you + base verb?",
  listOf("Please take a seat." to "لطفاً بنشینید.","Don't forget your ID." to "مدرک شناسایی‌ات را فراموش نکن.","Could you speak more slowly?" to "ممکن است آهسته‌تر صحبت کنید؟"),
  "پس از would you mind فعل ing می‌آید: Would you mind waiting?",
  "درخواست مؤدبانه کدام است؟",listOf("Give me that.","Could you pass me that, please?","You pass that."),1,"Could you درخواست مؤدبانه و طبیعی‌تری است."),
 lesson("B1","جمله‌سازی","جملات تعجبی و تأکید",
  "برای تأکید می‌توان از do/does/did در جمله مثبت استفاده کرد: I do understand. ساختارهای What a... و How... برای تعجب‌اند. در گفتار، آهنگ صدا نیز نقش مهمی دارد.",
  "do/does/did + base verb (emphasis) | What a + noun! | How + adjective!",
  listOf("I do appreciate your help." to "واقعاً از کمکت قدردانی می‌کنم.","What a great idea!" to "چه ایده عالی‌ای!"),
  "پس از did تأکیدی فعل پایه می‌آید: I did went نادرست است.",
  "برای تأکید: I ___ want to help.",listOf("do","am","have"),0,"do پیش از فعل پایه برای تأکید می‌آید."),
 lesson("B2","ساختار پیشرفته","وارونگی برای تأکید",
  "در سبک رسمی و تأکیدی، برخی قیدهای منفی یا محدودکننده در ابتدای جمله باعث وارونگی فعل کمکی و فاعل می‌شوند. این ساختار در نوشتار رسمی و سخنرانی دیده می‌شود.",
  "Never/Rarely/Not until + auxiliary + subject + verb",
  listOf("Never have I seen such a view." to "هرگز چنین منظره‌ای ندیده‌ام.","Not until Monday did we receive the results." to "تا دوشنبه نتایج را دریافت نکردیم."),
  "پس از قید منفی آغازین، ترتیب معمول فاعل و فعل کمکی وارونه می‌شود.",
  "Rarely ___ such a mistake.",listOf("I make","do I make","I do make"),1,"پس از rarely آغازین، وارونگی do I make لازم است."),
 lesson("B2","ساختار پیشرفته","تأکید با cleft sentences",
  "جمله‌های شکافته برای برجسته کردن بخشی از اطلاعات به‌کار می‌روند. ساختار It is/was ... that/who یک شخص یا بخش را برجسته می‌کند؛ what-clause نیز بر چیزی که مهم است تأکید می‌کند.",
  "It is/was + focus + that/who ... | What ... is ...",
  listOf("It was Anna who called." to "این آنا بود که زنگ زد.","What I need is a break." to "چیزی که نیاز دارم یک استراحت است."),
  "این ساختار برای تمرکز اطلاعات است؛ از آن بیش از حد در نثر ساده استفاده نکنید.",
  "___ I need is more time.",listOf("What","Which","That"),0,"What-clause به معنی چیزی که است."),
 lesson("B2","ساختار پیشرفته","حذف و جانشینی در گفتار",
  "برای جلوگیری از تکرار، بخش قابل‌فهم جمله حذف یا با so, not, do so, one/ones جایگزین می‌شود. این ساختارها در مکالمه طبیعی و نوشتار فشرده کاربرد دارند.",
  "so/not after think/hope | one/ones for countable noun replacement",
  listOf("I think so." to "فکر می‌کنم همین‌طور است.","I don't think so." to "فکر نمی‌کنم.","The blue one is cheaper." to "آن آبیه ارزان‌تر است."),
  "one/ones جای اسم قابل‌شمارش را می‌گیرند، نه اسم غیرقابل‌شمارش.",
  "I hope ___ . (امیدوارم همین‌طور باشد)",listOf("so","it","one"),0,"hope so برای جانشینی یک گزاره به‌کار می‌رود."),
 lesson("B2","ساختار پیشرفته","جمله‌های participle و کوتاه‌سازی موصولی",
  "عبارت‌های participle با شکل ing یا اسم مفعول می‌توانند جمله موصولی یا جمله وابسته را کوتاه کنند. فاعل ضمنی عبارت باید با فاعل جمله اصلی یکسان باشد تا dangling modifier ایجاد نشود.",
  "noun + present/past participle | While/When + participle phrase",
  listOf("The woman standing by the door is my boss." to "زنی که کنار در ایستاده رئیس من است.","Built in 1920, the house needs repairs." to "این خانه که در ۱۹۲۰ ساخته شده، نیاز به تعمیر دارد."),
  "نباید عبارتی مانند Walking down the street, the rain started بنویسیم؛ گویی باران راه می‌رفت.",
  "The documents ___ yesterday are on my desk.",listOf("signing","signed","sign"),1,"documents دریافت‌کننده عمل‌اند؛ اسم مفعول signed لازم است."),
 lesson("B2","ساختار پیشرفته","وجه التزامی و subjunctive",
  "پس از افعالی مانند suggest, recommend, insist و ساختارهای ضروری، در انگلیسی آمریکایی رسمی از شکل پایه فعل استفاده می‌شود؛ حتی برای سوم‌شخص مفرد. ساختار should نیز در برخی گونه‌های انگلیسی رایج است.",
  "suggest/recommend/insist + that + subject + base verb",
  listOf("They recommended that he see a doctor." to "توصیه کردند که او به پزشک مراجعه کند.","It's essential that she be informed." to "ضروری است که او مطلع شود."),
  "در این ساختار آمریکایی رسمی، he sees به‌جای he see می‌تواند ساختار را تغییر دهد.",
  "The doctor suggested that he ___ rest.",listOf("takes","take","taking"),1,"پس از suggested that، شکل پایه take می‌آید."),
 lesson("B2","ساختار پیشرفته","تطابق فاعل و فعل",
  "فعل باید با هسته فاعل از نظر مفرد و جمع هماهنگ باشد. عبارت‌های بین فاعل و فعل معمولاً توافق را تغییر نمی‌دهند. each/every و اسم‌های غیرقابل‌شمارش مفردند؛ a number of جمع و the number of مفرد است.",
  "singular subject + singular verb | plural subject + plural verb",
  listOf("The list of items is long." to "فهرست اقلام طولانی است.","A number of people are waiting." to "تعدادی از مردم منتظرند.","The number of applicants is rising." to "تعداد متقاضیان در حال افزایش است."),
  "فعل را با نزدیک‌ترین اسم اشتباه نگیرید؛ فاعل اصلی را پیدا کنید.",
  "The quality of these products ___ excellent.",listOf("are","is","be"),1,"هسته فاعل quality مفرد است؛ is درست است."),
 lesson("B2","ساختار پیشرفته","حروف ربط و پیوند منطقی",
  "برای پیوند ایده‌ها از and, but, or, so و پیوندهای وابسته مانند although, whereas, because, unless, provided that استفاده می‌کنیم. however و therefore قیدهای پیوندی‌اند و نشانه‌گذاری جداگانه می‌خواهند.",
  "Although + clause, clause | clause; however, clause | because + clause",
  listOf("Although it was late, we continued." to "با اینکه دیر بود، ادامه دادیم.","The road was closed; therefore, we took another route." to "جاده بسته بود؛ بنابراین مسیر دیگری رفتیم."),
  "although و but را برای اتصال همان دو بند با هم به‌کار نبرید: Although..., but... نادرست است.",
  "___ it was raining, they played.",listOf("Although","Because of","Despite of"),0,"although پیش از جمله کامل می‌آید."),
 lesson("B2","واژگان دستوری","determiners و کمیت‌نماهای پیشرفته",
  "تعیین‌کننده‌ها پیش از اسم می‌آیند و دامنه یا مرجع آن را مشخص می‌کنند: each/every, either/neither, both, all, whole, most, enough, several. انتخاب آن‌ها به مفرد/جمع و قابل‌شمارش بودن بستگی دارد.",
  "each/every + singular noun | both + plural | neither/either + singular (standard pattern)",
  listOf("Each employee has an ID." to "هر کارمند یک کارت شناسایی دارد.","Both options are available." to "هر دو گزینه در دسترس‌اند.","Neither answer is correct." to "هیچ‌یک از دو پاسخ درست نیست."),
  "each employees نادرست است؛ each employee درست است.",
  "___ of the two answers is acceptable.",listOf("Neither","Both","All"),0,"neither به معنی هیچ‌یک از دو مورد است و با فعل مفرد می‌آید."),
 lesson("B2","واژگان دستوری","کمیت‌نماهای دوگانه و دامنه معنی",
  "all, most, some, none و half می‌توانند با of و تعیین‌کننده مشخص بیایند: most people اما most of the people. تفاوت all day و the whole day و کاربرد none of بسته به اسم و سبک جمله بررسی می‌شود.",
  "quantifier + noun | quantifier + of + determiner + noun",
  listOf("Most students passed." to "بیشتر دانشجویان قبول شدند.","Most of the students passed." to "بیشتر آن دانشجویان قبول شدند."),
  "most the people نادرست است؛ most people یا most of the people درست است.",
  "کدام درست است؟",listOf("Most the people","Most people","Most of people (بدون مرجع مشخص)"),1,"most مستقیماً پیش از اسم جمع می‌آید."),
 lesson("C1","سبک و دقت","وجه‌نمایی و درجات قطعیت",
  "برای بیان قطعیت، احتمال، استنباط و احتیاط از modalها و قیدها استفاده می‌شود. must می‌تواند استنباط قوی، may/might احتمال و can't عدم امکان منطقی را نشان دهد. در گزارش محتاطانه از appears to, seems to و is likely to استفاده می‌شود.",
  "must/may/might/can't + base | must have/might have + past participle",
  listOf("She must be at work." to "احتمالاً حتماً سر کار است (استنباط قوی).","He might have missed the train." to "شاید قطار را از دست داده باشد."),
  "mustn't برای استنباط منفی به‌کار نمی‌رود؛ can't be مناسب‌تر است.",
  "The lights are off. They ___ be home.",listOf("must","can't","have to"),1,"خاموش بودن چراغ‌ها می‌تواند نشانه نبودن آن‌ها باشد؛ can't استنباط منفی است."),
 lesson("C1","سبک و دقت","ساختارهای پیچیده شرطی و وارونگی شرطی",
  "در سبک رسمی می‌توان if را حذف و فعل کمکی را به ابتدای بند شرط آورد: Had I known..., Were I..., Should you need.... این ساختارها رسمی‌تر از شکل معمول هستند.",
  "Had + subject + past participle | Were + subject ... | Should + subject + base",
  listOf("Had I known, I would have called." to "اگر می‌دانستم، زنگ می‌زدم.","Should you need help, contact us." to "اگر به کمک نیاز داشتید، با ما تماس بگیرید."),
  "این وارونگی بدون if رسمی است و نباید با سؤال اشتباه گرفته شود.",
  "___ you have any questions, let me know.",listOf("Should","Did","Have"),0,"Should you have معادل رسمی If you should have است."),
 lesson("C1","سبک و دقت","اسمی‌سازی و سبک رسمی",
  "در نوشتار دانشگاهی و اداری، فعل یا صفت به اسم تبدیل می‌شود: decide → decision، analyze → analysis. اسمی‌سازی متن را فشرده و رسمی می‌کند؛ اما زیاده‌روی آن جمله را سنگین و مبهم می‌سازد.",
  "verb/adjective → noun | make a decision / conduct an analysis",
  listOf("We decided to expand." to "تصمیم گرفتیم گسترش دهیم.","We made a decision to expand." to "تصمیمی برای گسترش گرفتیم."),
  "اسمی‌سازی بیش از حد می‌تواند فعل روشن را به عبارت سنگین تبدیل کند.",
  "اسم analyze چیست؟",listOf("analyzation (همیشه رایج‌ترین)","analysis","analyzingness"),1,"اسم رایج و استاندارد analyze برابر analysis است."),
 lesson("C1","سبک و دقت","ساختار موازی در جمله",
  "در فهرست‌ها و ساختارهای هم‌پایه، اجزای دستوری باید از نظر شکل هماهنگ باشند. parallelism خوانایی و وضوح را افزایش می‌دهد؛ به‌ویژه در نوشتار حرفه‌ای و دانشگاهی.",
  "A, B, and C must have parallel grammatical forms.",
  listOf("She likes reading, writing, and hiking." to "او مطالعه، نوشتن و پیاده‌روی را دوست دارد.","The job requires planning, organizing, and communicating." to "این شغل به برنامه‌ریزی، سازمان‌دهی و ارتباط نیاز دارد."),
  "reading, to write, and hiking ناهماهنگ است؛ شکل‌ها را یکسان کنید.",
  "کدام فهرست موازی است؟",listOf("to plan, organizing, and write","planning, organizing, and writing","plan, organizing, to write"),1,"هر سه جزء به شکل ing آمده‌اند."),
 lesson("C1","سبک و دقت","نشانه‌گذاری: ویرگول، نقطه‌ویرگول و دونقطه",
  "ویرگول برای جدا کردن اجزای فهرست، عبارت آغازین و بند وابسته مقدم به‌کار می‌رود. نقطه‌ویرگول دو جمله مستقل مرتبط را پیوند می‌دهد. دونقطه برای معرفی توضیح، فهرست یا نتیجه پس از یک بند کامل استفاده می‌شود.",
  "independent clause; independent clause | complete clause: explanation/list",
  listOf("After the meeting, we went home." to "پس از جلسه، به خانه رفتیم.","I had one goal: to finish on time." to "یک هدف داشتم: به‌موقع تمام کردن."),
  "دو جمله مستقل را فقط با ویرگول به هم نچسبانید؛ این خطای comma splice است.",
  "کدام نشانه دو جمله مستقل مرتبط را جدا می‌کند؟",listOf("semicolon","apostrophe","hyphen"),0,"نقطه‌ویرگول می‌تواند دو جمله مستقل مرتبط را جدا کند."),
 lesson("C2","دقت پیشرفته","ابهام‌زدایی و دامنه قیدها",
  "جایگاه قیدهایی مانند only, almost, even و just می‌تواند دامنه معنی جمله را تغییر دهد. قید را نزدیک بخشی قرار دهید که محدود یا برجسته می‌کند تا ابهام ایجاد نشود.",
  "Place a focusing adverb immediately before the element it modifies.",
  listOf("Only I told her." to "فقط من به او گفتم.","I told only her." to "من فقط به او گفتم (نه شخص دیگری).","I only told her." to "من فقط به او گفتم (کار دیگری نکردم/با تأکید وابسته به بافت)."),
  "only را بی‌توجه به منظور در هر جای جمله قرار ندهید؛ جایگاه، معنی را عوض می‌کند.",
  "برای معنی «فقط به او گفتم، نه به دیگران» کدام روشن‌تر است؟",listOf("Only I told her.","I told only her.","I only told."),1,"only پیش از her قرار گرفته و مفعول را محدود می‌کند."),
 lesson("C2","دقت پیشرفته","ساختارهای اسنادی و extraposition",
  "برای طبیعی‌تر کردن جمله‌های دارای فاعل طولانی، انگلیسی از it مقدماتی یا anticipatory it استفاده می‌کند: It is important to..., It seems that.... بند واقعی فاعل در انتهای جمله می‌آید.",
  "It + be/seem + adjective + to-infinitive/that-clause",
  listOf("It's important to check the details." to "مهم است که جزئیات را بررسی کنیم.","It seems that prices are rising." to "به نظر می‌رسد قیمت‌ها در حال افزایش‌اند."),
  "این it به شیء مشخص اشاره نمی‌کند؛ فاعل صوری است.",
  "___ is difficult to predict the outcome.",listOf("It","That","There"),0,"برای فاعل طولانی مصدر، it مقدماتی به‌کار می‌رود."),
 lesson("C2","دقت پیشرفته","تفاوت register: گفتاری، خنثی و رسمی",
  "انتخاب ساختار باید با مخاطب و موقعیت هماهنگ باشد. گفتار روزمره آمریکایی از شکل‌های کوتاه، افعال عبارتی و عبارت‌های ساده بهره می‌برد؛ نوشتار رسمی ممکن است از واژگان دقیق‌تر، بندهای موصولی کامل و اسمی‌سازی کنترل‌شده استفاده کند.",
  "Choose grammar and vocabulary according to audience, purpose and medium.",
  listOf("Could you help me out? (محاوره‌ای)" to "می‌تونی کمکم کنی؟","Would you be able to assist me? (رسمی‌تر)" to "آیا امکان دارد به من کمک کنید؟"),
  "رسمی‌تر بودن همیشه به معنی بهتر بودن نیست؛ وضوح و تناسب با موقعیت مهم است.",
  "برای گفت‌وگوی دوستانه کدام طبیعی‌تر است؟",listOf("I would be most grateful if you could...","Can you give me a hand?","I hereby request assistance."),1,"give me a hand در گفتار روزمره طبیعی و رایج است.")
)

@Composable
fun GrammarScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf<GrammarLesson?>(null) }
    var level by remember { mutableStateOf("همه") }
    val levels = listOf("همه","A1","A2","B1","B2","C1","C2")
    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC)).padding(horizontal = 18.dp, vertical = 12.dp)) {
        if (selected == null) {
            ScreenHeader("آموزش گرامر", onBack)
            Text("دستور زبان انگلیسی از پایه تا پیشرفته", color = Color(0xFF14213D), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(5.dp))
            Text("${grammarLessons.size} درس تألیفی با توضیح فارسی، مثال آمریکایی، خطاهای رایج و تمرین", color = Color(0xFF718096), fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                levels.forEach { item ->
                    FilterChip(selected = level == item, onClick = { level = item }, label = { Text(item, fontSize = 11.sp) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                grammarLessons.filter { level == "همه" || it.level == level }.forEach { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { selected = item },
                        shape = RoundedCornerShape(15.dp), color = Color.White, shadowElevation = 1.dp
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, color = Color(0xFF18243A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(item.category, color = Color(0xFF718096), fontSize = 11.sp)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF2563EB).copy(alpha = .10f)) {
                                Text(item.level, Modifier.padding(horizontal = 9.dp, vertical = 6.dp), color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
        Text(lesson.title, color = Color(0xFF14213D), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(5.dp))
        Text("${lesson.level}  •  ${lesson.category}", color = Color(0xFF2563EB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        GrammarPanel("توضیح کامل فارسی", lesson.explanation)
        GrammarPanel("ساختار و فرمول", lesson.structure)
        Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
            Column(Modifier.padding(15.dp)) {
                Text("مثال‌های کاربردی", color = Color(0xFF14213D), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Spacer(Modifier.height(9.dp))
                lesson.examples.forEach { (en, fa) ->
                    Text(en, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(fa, color = Color(0xFF64748B), fontSize = 12.sp)
                    Spacer(Modifier.height(9.dp))
                }
            }
        }
        GrammarPanel("اشتباه رایج", lesson.commonError)
        Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
            Column(Modifier.padding(15.dp)) {
                Text("تمرین این درس", color = Color(0xFF14213D), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Text(lesson.practice, color = Color(0xFF18243A), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                lesson.choices.forEachIndexed { index, choice ->
                    val bg = when {
                        answer == null -> Color(0xFFF4F7FC)
                        index == lesson.answer -> Color(0xFFE2F5ED)
                        answer == index -> Color(0xFFFFE8E8)
                        else -> Color(0xFFF4F7FC)
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable { answer = index },
                        shape = RoundedCornerShape(10.dp), color = bg
                    ) { Text(choice, Modifier.padding(12.dp), color = Color(0xFF18243A), fontSize = 13.sp) }
                }
                if (answer != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(if (answer == lesson.answer) "آفرین! پاسخ درست است." else "پاسخ صحیح: ${lesson.choices[lesson.answer]}", color = if (answer == lesson.answer) Color(0xFF0F8B66) else Color(0xFFB54747), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(lesson.reason, color = Color(0xFF64748B), fontSize = 12.sp)
                    TextButton(onClick = { answer = null }) { Text("تکرار تمرین") }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GrammarPanel(title: String, body: String) {
    Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = Color(0xFF14213D), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(body, color = Color(0xFF18243A), fontSize = 13.sp, lineHeight = 23.sp)
        }
    }
}
