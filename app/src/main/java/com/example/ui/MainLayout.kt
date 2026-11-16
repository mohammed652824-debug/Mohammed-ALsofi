package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.GeneratedContent
import com.example.data.Student
import com.example.data.TeacherLog
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: EducationViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0: الرئيسية (Dashboard), 1: السجلات والتحليل (Students/Analytics), 2: ملف الإنجاز (Teacher Logs), 3: أرشيف المواد (History)

    var showAddStudent by remember { mutableStateOf(false) }
    var showAddTeacherLog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }

    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val contents by viewModel.allContents.collectAsStateWithLifecycle()
    val teacherLogs by viewModel.allTeacherLogs.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "منصة المعلم الذكي",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        // Saudi Arabia badge
                        Surface(
                            color = SaudiGreenLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المملكة العربية السعودية 🇸🇦",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaudiGreenPrimary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = SaudiGreenLight
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "السجلات والتحليل") },
                    label = { Text("تحليل النتائج", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = SaudiGreenLight
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "ملف الأداء") },
                    label = { Text("ملف الإنجاز", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = SaudiGreenLight
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "أرشيف المواد") },
                    label = { Text("المواد المحفوظة", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = SaudiGreenLight
                    )
                )
            }
        }
    ) { innerPadding ->
        // Navigation / Tab handling
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onViewGeneratedResult = { showDetailDialog = true }
                )
                1 -> AnalyticsScreen(
                    viewModel = viewModel,
                    students = students,
                    onAddStudentClick = { showAddStudent = true }
                )
                2 -> PortfolioScreen(
                    viewModel = viewModel,
                    logs = teacherLogs,
                    onAddLogClick = { showAddTeacherLog = true }
                )
                3 -> ArchiveScreen(
                    viewModel = viewModel,
                    contents = contents,
                    onViewContent = { showDetailDialog = true }
                )
            }

            // ADD STUDENT DIALOG
            if (showAddStudent) {
                AddStudentDialog(
                    viewModel = viewModel,
                    onDismiss = { showAddStudent = false },
                    onConfirm = {
                        viewModel.saveStudent()
                        showAddStudent = false
                        Toast.makeText(context, "تم حفظ الطالب بنجاح", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // ADD TEACHER LOG DIALOG
            if (showAddTeacherLog) {
                AddTeacherLogDialog(
                    viewModel = viewModel,
                    onDismiss = { showAddTeacherLog = false },
                    onConfirm = {
                        viewModel.saveTeacherLog()
                        showAddTeacherLog = false
                        Toast.makeText(context, "تم تسجيل بند الإنجاز بنجاح", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // FULLSCREEN GENERATED MATERIAL VIEW DIALOG
            if (showDetailDialog && viewModel.activeGeneratedContentJson.isNotEmpty()) {
                GeneratedMaterialDialog(
                    title = viewModel.activeGeneratedTitle,
                    type = viewModel.activeGeneratedType,
                    jsonStr = viewModel.activeGeneratedContentJson,
                    onDismiss = { showDetailDialog = false }
                )
            }
        }
    }
}

// ---------------------- 1. DASHBOARD SCREEN ----------------------

@Composable
fun DashboardScreen(
    viewModel: EducationViewModel,
    onViewGeneratedResult: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End // RTL layout orientation
    ) {
        // Hero Branding Card with Beautiful Green Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(SaudiGreenPrimary, Color(0xFF047857))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "منصتك التعليمية الذكية المتكاملة",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "صناعة كل أعمال المعلم بالذكاء الاصطناعي",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "أنشئ عروض الباوربوينت، بنوك الأسئلة، الاختبارات القبيلية والبعدية، أوراق العمل المخصصة، والخطط العلاجية المتكاملة في ثوانٍ معدودة مجاناً بالكامل.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    lineHeight = 18.sp
                )
            }
        }

        // Section header
        Text(
            text = "خطوة 1: أدخل موضوع الدرس والبيانات",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )

        // Topic TextField
        OutlinedTextField(
            value = viewModel.topicInput,
            onValueChange = { viewModel.topicInput = it },
            label = { Text("أدخل موضوع الدرس أو المفهوم (مثال: دورة حياة النبات، جدول الضرب، همزة الوصل)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("topic_input"),
            singleLine = true,
            trailingIcon = {
                Icon(
                    Icons.Default.Article,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Stage Selection & Subject Selection Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stage Selection Card
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "المرحلة الدراسية:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("ثانوي", "متوسط", "ابتدائي").forEach { stage ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (viewModel.selectedStage == stage) SaudiGreenPrimary else Color.Transparent)
                                .clickable { viewModel.selectedStage = stage },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.selectedStage == stage) Color.White else SlateDarkSecondary
                            )
                        }
                    }
                }
            }

            // Subject Selector Dropdown
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "المادة الدراسية:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                        .clickable { expanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = viewModel.selectedSubject,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDarkSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        listOf("الرياضيات", "العلوم", "اللغة العربية", "الدراسات الإسلامية", "اللغة الإنجليزية", "الحاسب الآلي").forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                                onClick = {
                                    viewModel.selectedSubject = subject
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // AI Loading State Indicator
        if (viewModel.isGenerating) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SaudiGreenLight),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = SaudiGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "جاري صياغة المحتوى الذكي بالكامل مع نموذج Gemini... يرجى الانتظار",
                        fontSize = 12.sp,
                        color = SaudiGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }

        // Error Banner
        viewModel.errorMsg?.let { error ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "حدث خطأ: $error. تم تفعيل المولد الذكي المحلي البديل بنجاح لتوفير المادة مجاناً.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Right
                )
            }
        }

        // Action Options Grid Header
        Text(
            text = "خطوة 2: اختر الخدمة الذكية المراد توليدها",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )

        // Services Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceCard(
                    title = "عروض الباوربوينت",
                    description = "توليد فوري لشرائح الدرس مع العناوين والفقرات وملاحظات الشرح.",
                    icon = Icons.Default.Slideshow,
                    badgeText = "تلقائي وبدقة",
                    badgeColor = SaudiGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (viewModel.topicInput.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال موضوع الدرس أولاً", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generatePPT()
                            onViewGeneratedResult()
                        }
                    }
                )
                ServiceCard(
                    title = "إنشاء الاختبارات الذكية",
                    description = "أسئلة اختيار من متعدد مع الإجابات النموذجية والشروح التفصيلية.",
                    icon = Icons.Default.Quiz,
                    badgeText = "جاهز فورمز",
                    badgeColor = CustomBlueAccent,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (viewModel.topicInput.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال موضوع الدرس أولاً", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generateQuiz()
                            onViewGeneratedResult()
                        }
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceCard(
                    title = "أوراق العمل التفاعلية",
                    description = "تمارين متنوعة وتدريبات مع نموذج الإجابة للمعلم للتصحيح السريع.",
                    icon = Icons.Default.Description,
                    badgeText = "تسهيل المتابعة",
                    badgeColor = WarmGold,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (viewModel.topicInput.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال موضوع الدرس أولاً", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generateWorksheet()
                            onViewGeneratedResult()
                        }
                    }
                )
                ServiceCard(
                    title = "خطط النشاط الطلابي",
                    description = "إعداد خطط النشاط المدرسي والبرنامج الزمني الموصى به للمرحلة.",
                    icon = Icons.Default.Groups,
                    badgeText = "لا صفي ممتع",
                    badgeColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (viewModel.topicInput.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال موضوع الدرس أولاً", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generateActivityPlan()
                            onViewGeneratedResult()
                        }
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ServiceCard(
                    title = "الخطة العلاجية",
                    description = "دعم مكثف وتوجيهي للطلاب المتعثرين لتمكينهم من استدراك المهارات.",
                    icon = Icons.Default.OfflineBolt,
                    badgeText = "دعم المتعثرين",
                    badgeColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (viewModel.topicInput.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال موضوع الدرس أولاً", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generatePlan(isRemedial = true)
                            onViewGeneratedResult()
                        }
                    }
                )
                ServiceCard(
                    title = "الخطة الإثرائية",
                    description = "أنشطة وتحديات مميزة ومشاريع لتطوير ذكاء وقدرات الطلاب الموهوبين.",
                    icon = Icons.Default.Star,
                    badgeText = "رعاية الموهوبين",
                    badgeColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (viewModel.topicInput.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال موضوع الدرس أولاً", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generatePlan(isRemedial = false)
                            onViewGeneratedResult()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Platform Statistics Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+200,000", fontWeight = FontWeight.Bold, color = SaudiGreenPrimary, fontSize = 16.sp)
                    Text("طالب مستفيد", fontSize = 11.sp, color = SlateMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+50,000", fontWeight = FontWeight.Bold, color = CustomBlueAccent, fontSize = 16.sp)
                    Text("اختبار تم إنشاؤه", fontSize = 11.sp, color = SlateMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("+20,000", fontWeight = FontWeight.Bold, color = WarmGold, fontSize = 16.sp)
                    Text("عرض تقديمي", fontSize = 11.sp, color = SlateMedium)
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                // Icon
                Surface(
                    color = SaudiGreenLight,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = SaudiGreenPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDarkSecondary,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = SlateMedium,
                    textAlign = TextAlign.Right,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ---------------------- 2. ANALYTICS & STUDENTS SCREEN ----------------------

@Composable
fun AnalyticsScreen(
    viewModel: EducationViewModel,
    students: List<Student>,
    onAddStudentClick: () -> Unit
) {
    val context = LocalContext.current
    val averageScore = if (students.isNotEmpty()) students.map { it.score }.average() else 0.0
    val successRate = if (students.isNotEmpty()) {
        (students.filter { it.score >= 50.0 }.size.toDouble() / students.size.toDouble()) * 100.0
    } else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Analytics Summary Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "تحليل نتائج الاختبارات وسجلات الفصول 📊",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${students.size}",
                                color = SkyAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("الطلاب المسجلين", fontSize = 11.sp, color = DarkTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f".format(averageScore),
                                color = SaudiGreenDarkPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("متوسط درجات الطلاب", fontSize = 11.sp, color = DarkTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.0f%%".format(successRate),
                                color = WarmGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("نسبة النجاح العامة", fontSize = 11.sp, color = DarkTextSecondary)
                        }
                    }
                }
            }
        }

        // Custom Canvas Visual Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "مخطط توزيع الدرجات الفوري للفصل",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SlateDarkSecondary,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (students.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "يرجى تسجيل الطلاب لعرض مخطط توزيع الدرجات التفصيلي.",
                                fontSize = 12.sp,
                                color = SlateMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Grade brackets count
                        val excellentCount = students.filter { it.score >= 90.0 }.size
                        val veryGoodCount = students.filter { it.score in 80.0..89.9 }.size
                        val goodCount = students.filter { it.score in 65.0..79.9 }.size
                        val passCount = students.filter { it.score in 50.0..64.9 }.size
                        val failCount = students.filter { it.score < 50.0 }.size

                        val maxCount = maxOf(excellentCount, veryGoodCount, goodCount, passCount, failCount, 1)

                        // Compose custom Canvas to draw visual bar graph
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            val spacing = 24.dp.toPx()
                            val barWidth = 32.dp.toPx()
                            val canvasHeight = size.height - 30.dp.toPx()
                            val canvasWidth = size.width
                            val totalBarsCount = 5
                            val availableWidthForBars = canvasWidth - (spacing * (totalBarsCount + 1))
                            val computedBarWidth = (availableWidthForBars / totalBarsCount).coerceAtMost(50.dp.toPx())

                            val brackets = listOf(
                                Pair("ممتاز (>=90)", excellentCount),
                                Pair("جيد جداً", veryGoodCount),
                                Pair("جيد", goodCount),
                                Pair("مقبول", passCount),
                                Pair("ضعيف (<50)", failCount)
                            )

                            // Grid Lines
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val y = (canvasHeight / gridLines) * i
                                drawLine(
                                    color = Color(0xFFE2E8F0),
                                    start = Offset(0f, y),
                                    end = Offset(canvasWidth, y),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            }

                            brackets.forEachIndexed { index, bracket ->
                                val count = bracket.second
                                val barHeight = (count.toFloat() / maxCount.toFloat()) * canvasHeight
                                val x = spacing + index * (computedBarWidth + spacing)
                                val y = canvasHeight - barHeight

                                // Draw bar with gradient colors
                                val colors = when (index) {
                                    0 -> listOf(SaudiGreenPrimary, Color(0xFF10B981))
                                    1 -> listOf(CustomBlueAccent, Color(0xFF60A5FA))
                                    2 -> listOf(WarmGold, Color(0xFFFBBF24))
                                    3 -> listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
                                    else -> listOf(Color(0xFFEF4444), Color(0xFFF87171))
                                }

                                drawRoundRect(
                                    brush = Brush.verticalGradient(colors),
                                    topLeft = Offset(x, y),
                                    size = Size(computedBarWidth, barHeight),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )

                                // Draw Count text above the bar
                                drawContext.canvas.nativeCanvas.apply {
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.DKGRAY
                                        textSize = 10.dp.toPx()
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isFakeBoldText = true
                                    }
                                    drawText(
                                        "$count طالباً",
                                        x + computedBarWidth / 2,
                                        y - 8f,
                                        paint
                                    )

                                    // Bracket label text below bar
                                    val labelPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.GRAY
                                        textSize = 9.dp.toPx()
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    drawText(
                                        bracket.first,
                                        x + computedBarWidth / 2,
                                        canvasHeight + 16.dp.toPx(),
                                        labelPaint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section header with Add student button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onAddStudentClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = SaudiGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_student_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("تسجيل طالب جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "سجل درجات الطلاب وأعمال السنة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateDarkSecondary
                )
            }
        }

        // Student List Item Registry
        if (students.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا يوجد طلاب مسجلين حالياً. اضغط على الزر أعلاه لإضافة أول طالب في فصلك.",
                        color = SlateMedium,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(students) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Delete Button
                        IconButton(
                            onClick = {
                                viewModel.deleteStudent(student.id)
                                Toast.makeText(context, "تم حذف سجل الطالب", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444))
                        }

                        // Score indicator
                        Surface(
                            color = when {
                                student.score >= 90.0 -> SaudiGreenLight
                                student.score >= 75.0 -> Color(0xFFEFF6FF)
                                student.score >= 50.0 -> Color(0xFFFFFBEB)
                                else -> Color(0xFFFEF2F2)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "الدرجة: ${student.score.toInt()}%",
                                color = when {
                                    student.score >= 90.0 -> SaudiGreenPrimary
                                    student.score >= 75.0 -> CustomBlueAccent
                                    student.score >= 50.0 -> WarmGold
                                    else -> Color(0xFFEF4444)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Student Name and details
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateDarkSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${student.stage} - ${student.grade}",
                                fontSize = 11.sp,
                                color = SlateMedium
                            )
                            if (student.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = student.notes,
                                    fontSize = 11.sp,
                                    color = SlateMedium,
                                    textAlign = TextAlign.Right,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Avatar
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = SlateDarkSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 3. PORTFOLIO SCREEN ----------------------

@Composable
fun PortfolioScreen(
    viewModel: EducationViewModel,
    logs: List<TeacherLog>,
    onAddLogClick: () -> Unit
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Teacher Portfolio Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SaudiGreenPrimary, CustomBlueAccent)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ملف الأداء الوظيفي وإنجازات المعلم 📝",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "سجل دوراتك التدريبية، مبادراتك المدرسية، وإنجازاتك السنوية لإنشاء ملف أداء مهني متكامل وموثق تلقائياً.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Export summary button
                    Button(
                        onClick = {
                            if (logs.isEmpty()) {
                                Toast.makeText(context, "الرجاء إضافة بعض المنجزات أولاً لتصدير التقرير", Toast.LENGTH_SHORT).show()
                            } else {
                                val report = StringBuilder()
                                report.append("=== ملف إنجاز المعلم والأداء الوظيفي السنوي ===\n")
                                report.append("تم التوليد بنجاح عبر منصة المعلم الذكي\n")
                                report.append("تاريخ التقرير: 2026-07-20\n\n")
                                logs.forEachIndexed { index, log ->
                                    val typeStr = when(log.type) {
                                        "course" -> "دورة تدريبية"
                                        "initiative" -> "مبادرة تعليمية"
                                        else -> "بند إنجاز مهني"
                                    }
                                    report.append("${index + 1}. $typeStr: ${log.title}\n")
                                    report.append("   التاريخ: ${log.date}\n")
                                    report.append("   الوصف: ${log.description}\n")
                                    report.append("   التقييم الذاتي: ${log.score}/100\n\n")
                                }
                                
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Teacher Portfolio", report.toString())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ تقرير الأداء الوظيفي الكامل إلى الحافظة لطباعته ومشاركته!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = SaudiGreenPrimary, modifier = Modifier.size(16.dp))
                            Text("تصدير ملف الأداء الكامل", fontSize = 11.sp, color = SaudiGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section header with Add Log button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onAddLogClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = SaudiGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_log_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("إضافة إنجاز جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "سجل الإنجازات الحالية",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateDarkSecondary
                )
            }
        }

        // List of teacher logs
        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "سجل إنجازاتك فارغ حالياً. سجل دوراتك الحاضرة أو المبادرات للتوثيق السنوي.",
                        color = SlateMedium,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteTeacherLog(log.id)
                                    Toast.makeText(context, "تم حذف البند بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = when (log.type) {
                                        "course" -> SaudiGreenLight
                                        "initiative" -> Color(0xFFEFF6FF)
                                        else -> Color(0xFFFFFBEB)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when(log.type) {
                                            "course" -> "دورة تدريبية"
                                            "initiative" -> "مبادرة مدرسية"
                                            else -> "منجز مهني"
                                        },
                                        color = when(log.type) {
                                            "course" -> SaudiGreenPrimary
                                            "initiative" -> CustomBlueAccent
                                            else -> WarmGold
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = log.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SlateDarkSecondary,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = log.description,
                            fontSize = 12.sp,
                            color = SlateMedium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "التقييم الذاتي: ${log.score}/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaudiGreenPrimary
                            )
                            Text(
                                text = "تاريخ التسجيل: ${log.date}",
                                fontSize = 11.sp,
                                color = SlateMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 4. ARCHIVE SCREEN ----------------------

@Composable
fun ArchiveScreen(
    viewModel: EducationViewModel,
    contents: List<GeneratedContent>,
    onViewContent: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        item {
            Text(
                text = "أرشيف المواد التعليمية المحفوظة 📂",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SlateDarkSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }

        if (contents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد مواد تعليمية منشأة حالياً. عد لعلامة الرئيسية وأنشئ أول مادة دراسية.",
                        color = SlateMedium,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(contents) { content ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.activeGeneratedTitle = content.title
                            viewModel.activeGeneratedType = content.type
                            viewModel.activeGeneratedContentJson = content.contentJson
                            onViewContent()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.deleteContent(content.id)
                                Toast.makeText(context, "تم حذف المادة بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف المادة", tint = Color(0xFFEF4444))
                        }

                        // Arrow indicator
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "عرض التفاصيل",
                            tint = SlateMedium
                        )

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = when (content.type) {
                                        "ppt" -> SaudiGreenLight
                                        "quiz" -> Color(0xFFEFF6FF)
                                        "worksheet" -> Color(0xFFFFFBEB)
                                        else -> Color(0xFFFEF2F2)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when(content.type) {
                                            "ppt" -> "باوربوينت"
                                            "quiz" -> "اختبار"
                                            "worksheet" -> "ورقة عمل"
                                            "remedial" -> "خطة علاجية"
                                            "enrichment" -> "خطة إثرائية"
                                            else -> "نشاط طلابي"
                                        },
                                        color = when(content.type) {
                                            "ppt" -> SaudiGreenPrimary
                                            "quiz" -> CustomBlueAccent
                                            "worksheet" -> WarmGold
                                            else -> Color(0xFFEF4444)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = content.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SlateDarkSecondary,
                                    textAlign = TextAlign.Right
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "المرحلة: ${content.stage} | المادة: ${content.subject}",
                                fontSize = 11.sp,
                                color = SlateMedium,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- DIALOG COMPONENTS ----------------------

@Composable
fun AddStudentDialog(
    viewModel: EducationViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "سجل طالباً جديداً بالفصل 👤",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SlateDarkSecondary,
                    textAlign = TextAlign.Right
                )
                
                OutlinedTextField(
                    value = viewModel.studentNameInput,
                    onValueChange = { viewModel.studentNameInput = it },
                    label = { Text("اسم الطالب بالكامل") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_name_input")
                )

                OutlinedTextField(
                    value = viewModel.studentGradeInput,
                    onValueChange = { viewModel.studentGradeInput = it },
                    label = { Text("الصف الدراسي (مثال: الصف الثالث)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_grade_input")
                )

                OutlinedTextField(
                    value = viewModel.studentScoreInput,
                    onValueChange = { viewModel.studentScoreInput = it },
                    label = { Text("درجة أعمال السنة الحالية (100-0)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("student_score_input")
                )

                OutlinedTextField(
                    value = viewModel.studentNotesInput,
                    onValueChange = { viewModel.studentNotesInput = it },
                    label = { Text("ملاحظات الأداء (احتياج علاجي / إثرائي)") },
                    modifier = Modifier.fillMaxWidth().testTag("student_notes_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = SlateMedium)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = SaudiGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_save_student")
                    ) {
                        Text("حفظ البيانات")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTeacherLogDialog(
    viewModel: EducationViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "إضافة بند لملف الإنجاز 📝",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SlateDarkSecondary,
                    textAlign = TextAlign.Right
                )

                OutlinedTextField(
                    value = viewModel.logTitleInput,
                    onValueChange = { viewModel.logTitleInput = it },
                    label = { Text("عنوان الإنجاز (دورة، مبادرة، شهادة شكر)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("log_title_input")
                )

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text("تصنيف المنشط المنجز:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Pair("course", "دورة"),
                            Pair("initiative", "مبادرة"),
                            Pair("achievement", "أخرى")
                        ).forEach { pair ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (viewModel.logTypeInput == pair.first) SaudiGreenPrimary else Color.Transparent)
                                    .clickable { viewModel.logTypeInput = pair.first },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pair.second,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.logTypeInput == pair.first) Color.White else SlateDarkSecondary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.logDescInput,
                    onValueChange = { viewModel.logDescInput = it },
                    label = { Text("وصف مختصر للإنجاز والأثر التربوي") },
                    modifier = Modifier.fillMaxWidth().testTag("log_desc_input")
                )

                OutlinedTextField(
                    value = viewModel.logScoreInput,
                    onValueChange = { viewModel.logScoreInput = it },
                    label = { Text("نسبة الإنجاز أو التقييم الذاتي (100-0)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("log_score_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = SlateMedium)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = SaudiGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_save_log")
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedMaterialDialog(
    title: String,
    type: String,
    jsonStr: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Parse JSON safely outside the composition tree
    val json = remember(jsonStr) {
        try {
            JSONObject(jsonStr)
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SaudiGreenPrimary,
                        textAlign = TextAlign.Right
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Content Pane
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dynamically render PPT / Quiz / Worksheet structure
                        if (json != null) {
                            when (type) {
                                "ppt" -> {
                                    val slides = json.getJSONArray("slides")
                                    for (i in 0 until slides.length()) {
                                        val slide = slides.getJSONObject(i)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = SaudiGreenLight.copy(alpha = 0.5f))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = slide.getString("title"),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = SaudiGreenPrimary,
                                                    textAlign = TextAlign.Right
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                val contentArray = slide.getJSONArray("content")
                                                for (j in 0 until contentArray.length()) {
                                                    Text(
                                                        text = "• " + contentArray.getString(j),
                                                        fontSize = 12.sp,
                                                        color = SlateDarkSecondary,
                                                        textAlign = TextAlign.Right,
                                                        modifier = Modifier.padding(bottom = 2.dp)
                                                    )
                                                }
                                                Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)
                                                Text(
                                                    text = "💡 ملاحظات المعلم: " + slide.getString("notes"),
                                                    fontSize = 11.sp,
                                                    color = SlateMedium,
                                                    textAlign = TextAlign.Right,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                "quiz" -> {
                                    val questions = json.getJSONArray("questions")
                                    for (i in 0 until questions.length()) {
                                        val q = questions.getJSONObject(i)
                                        val options = q.getJSONArray("options")
                                        val correctIdx = q.getInt("correct")

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = "سؤال ${i+1}: " + q.getString("question"),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = SlateDarkSecondary,
                                                    textAlign = TextAlign.Right
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                for (j in 0 until options.length()) {
                                                    val isCorrect = j == correctIdx
                                                    Surface(
                                                        color = if (isCorrect) SaudiGreenLight else Color.White,
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, if (isCorrect) SaudiGreenPrimary else Color(0xFFE2E8F0)),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = options.getString(j) + if (isCorrect) " (✓ الإجابة الصحيحة)" else "",
                                                            fontSize = 12.sp,
                                                            color = if (isCorrect) SaudiGreenPrimary else SlateDarkSecondary,
                                                            modifier = Modifier.padding(8.dp),
                                                            textAlign = TextAlign.Right
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "💡 التفسير والتعليم: " + q.getString("explanation"),
                                                    fontSize = 11.sp,
                                                    color = SlateMedium,
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                        }
                                    }
                                }

                                "worksheet" -> {
                                    Text(
                                        text = "التعليمات: " + json.getString("instructions"),
                                        fontSize = 12.sp,
                                        color = SlateMedium,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    val tasks = json.getJSONArray("tasks")
                                    Text(
                                        text = "تمارين الطالب:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SaudiGreenPrimary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    for (i in 0 until tasks.length()) {
                                        val task = tasks.getJSONObject(i)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                                                    Text(task.getString("grade"), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                                    Text("(${task.getString("type")})", fontSize = 11.sp, color = SaudiGreenPrimary, fontWeight = FontWeight.Bold)
                                                    Text(task.getString("question"), fontSize = 13.sp, color = SlateDarkSecondary, textAlign = TextAlign.Right)
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "مفتاح الإجابة النموذجي للمعلم:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CustomBlueAccent,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    val answers = json.getJSONArray("answer_key")
                                    for (i in 0 until answers.length()) {
                                        Text(
                                            text = "• " + answers.getString(i),
                                            fontSize = 12.sp,
                                            color = SlateMedium,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                "remedial", "enrichment" -> {
                                    Text(
                                        text = "الفئة المستهدفة: " + json.getString("target_group"),
                                        fontSize = 12.sp,
                                        color = SaudiGreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "الأهداف التعليمية للخطة:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SlateDarkSecondary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    val objectives = json.getJSONArray("objectives")
                                    for (i in 0 until objectives.length()) {
                                        Text(
                                            text = "- " + objectives.getString(i),
                                            fontSize = 12.sp,
                                            color = SlateMedium,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "الأنشطة الموصى بها للتنفيذ:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SaudiGreenPrimary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    val activities = json.getJSONArray("activities")
                                    for (i in 0 until activities.length()) {
                                        val act = activities.getJSONObject(i)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                                Text(act.getString("title"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDarkSecondary)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(act.getString("description"), fontSize = 12.sp, color = SlateMedium, textAlign = TextAlign.Right)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("المدة المقترحة: " + act.getString("duration"), fontSize = 11.sp, color = SaudiGreenPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "طريقة قياس الأثر والتقييم البعدي:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CustomBlueAccent,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    Text(
                                        text = json.getString("evaluation_method"),
                                        fontSize = 12.sp,
                                        color = SlateMedium,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                else -> {
                                    // Activity plan
                                    Text(
                                        text = json.getString("description"),
                                        fontSize = 12.sp,
                                        color = SlateMedium,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "الأهداف التربوية للنشاط:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SlateDarkSecondary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    val objectives = json.getJSONArray("objectives")
                                    for (i in 0 until objectives.length()) {
                                        Text(
                                            text = "• " + objectives.getString(i),
                                            fontSize = 12.sp,
                                            color = SlateMedium,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "مراحل التنفيذ والفقرات:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SaudiGreenPrimary,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    val phases = json.getJSONArray("phases")
                                    for (i in 0 until phases.length()) {
                                        val phase = phases.getJSONObject(i)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                                Text(phase.getString("name"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDarkSecondary)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(phase.getString("tasks"), fontSize = 12.sp, color = SlateMedium, textAlign = TextAlign.Right)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "المتطلبات والميزانية الموصى بها:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CustomBlueAccent,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                    Text(
                                        text = json.getString("requirements"),
                                        fontSize = 12.sp,
                                        color = SlateMedium,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        } else {
                            // Fallback raw text viewing if JSON parsing fails
                            Text(
                                text = jsonStr,
                                fontSize = 12.sp,
                                color = SlateDarkSecondary,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action buttons: Copy, and Google Forms format
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export to Google Forms Button (Only for Quizzes!)
                    if (type == "quiz") {
                        Button(
                            onClick = {
                                try {
                                    val quizJson = JSONObject(jsonStr)
                                    val questions = quizJson.getJSONArray("questions")
                                    val formsFormat = StringBuilder()
                                    formsFormat.append("=== نموذج استيراد مباشر لاختبار Google Forms ===\n")
                                    formsFormat.append("توجيهات للمعلم: انسخ هذا النص كاملاً، واستخدم أداة 'Form Presenter' أو قم بلصقه مباشرة في الحقول:\n\n")
                                    
                                    for (i in 0 until questions.length()) {
                                        val q = questions.getJSONObject(i)
                                        formsFormat.append("السؤال ${i+1}: ${q.getString("question")}\n")
                                        val opts = q.getJSONArray("options")
                                        for (j in 0 until opts.length()) {
                                            formsFormat.append(" [${if (j == q.getInt("correct")) "X" else " "}] ${opts.getString(j)}\n")
                                        }
                                        formsFormat.append(" التوضيح العلمي: ${q.getString("explanation")}\n\n")
                                    }
                                    
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Google Forms Quiz", formsFormat.toString())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم توليد وتنسيق الاختبار بصيغة Google Forms في الحافظة لسهولة الإرسال للطلاب!", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "فشل توليد صيغة جوجل فورمز", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CustomBlueAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("تصدير بصيغة جوجل فورمز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Standard Copy Button
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Educational Material", jsonStr)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ المادة التعليمية كاملة إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaudiGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("copy_content_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("نسخ المادة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
