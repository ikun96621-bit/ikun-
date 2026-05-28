package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.Category
import com.example.data.Question
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IkunApp(viewModel: IkunViewModel) {
    val currentScreen = viewModel.currentScreen
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Display Mascot in top bar
                        AsyncImage(
                            model = R.drawable.ic_launcher_foreground,
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F7))
                        )
                        Text(
                            text = when (viewModel.currentScreen) {
                                AppScreen.DASHBOARD -> "ikun极速刷题"
                                AppScreen.PRACTICE -> "分类刷题强化"
                                AppScreen.MOCK_EXAM_SETUP -> "自定义模拟考试"
                                AppScreen.MOCK_EXAM_ACTIVE -> "模拟机考考试中..."
                                AppScreen.MOCK_EXAM_RESULT -> "考试报告单"
                                AppScreen.ERROR_BOOK -> "我的错题本"
                                AppScreen.ERROR_BOOK_REDO -> "错题攻坚重做"
                                AppScreen.SETTINGS -> "AI 接口配置"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    if (viewModel.currentScreen != AppScreen.DASHBOARD) {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Do not show navigation bar while an exam is actively running to prevent escaping
            if (viewModel.currentScreen != AppScreen.MOCK_EXAM_ACTIVE) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = viewModel.currentScreen == AppScreen.DASHBOARD || viewModel.currentScreen == AppScreen.PRACTICE,
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        icon = { Icon(Icons.Default.School, contentDescription = "题库") },
                        label = { Text("学堂") }
                    )
                    NavigationBarItem(
                        selected = viewModel.currentScreen == AppScreen.MOCK_EXAM_SETUP || viewModel.currentScreen == AppScreen.MOCK_EXAM_ACTIVE || viewModel.currentScreen == AppScreen.MOCK_EXAM_RESULT,
                        onClick = { viewModel.navigateTo(AppScreen.MOCK_EXAM_SETUP) },
                        icon = { Icon(Icons.Default.Timer, contentDescription = "模考") },
                        label = { Text("模考") }
                    )
                    NavigationBarItem(
                        selected = viewModel.currentScreen == AppScreen.ERROR_BOOK || viewModel.currentScreen == AppScreen.ERROR_BOOK_REDO,
                        onClick = { viewModel.navigateTo(AppScreen.ERROR_BOOK) },
                        icon = { Icon(Icons.Default.Error, contentDescription = "错题") },
                        label = { Text("错题本") }
                    )
                    NavigationBarItem(
                        selected = viewModel.currentScreen == AppScreen.SETTINGS,
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "接口") },
                        label = { Text("设置") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = viewModel.currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel)
                    AppScreen.PRACTICE -> PracticeScreen(viewModel)
                    AppScreen.MOCK_EXAM_SETUP -> MockExamSetupScreen(viewModel)
                    AppScreen.MOCK_EXAM_ACTIVE -> MockExamActiveScreen(viewModel)
                    AppScreen.MOCK_EXAM_RESULT -> MockExamResultScreen(viewModel)
                    AppScreen.ERROR_BOOK -> ErrorBookScreen(viewModel)
                    AppScreen.ERROR_BOOK_REDO -> ErrorBookRedoScreen(viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

// --- SCREEN 1: DASHBOARD ---
@Composable
fun DashboardScreen(viewModel: IkunViewModel) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val wrongQuestions by viewModel.wrongQuestions.collectAsStateWithLifecycle()

    // Calculate details
    val totalAnswered = progress.size
    val correctCount = progress.count { it.isCorrect }
    val accuracy = if (totalAnswered > 0) (correctCount * 100) / totalAnswered else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Welcoming card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KunerCharcoal)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "你好，最强练习生！",
                            color = KunerOrange,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "练习时长两年半，今天你刷题了吗？拿出篮球般的极速拼搏劲头，开启刷题风暴！",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Basket logo icon
                    Icon(
                        imageVector = Icons.Default.SportsBasketball,
                        contentDescription = "Basketball Icon",
                        tint = KunerOrange,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        // Stats card
        item {
            Text(
                text = "📊 数据统计",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("累计刷题", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalAnswered 题", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp).align(Alignment.CenterVertically))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("平均准确率", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$accuracy %", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2ECC71))
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp).align(Alignment.CenterVertically))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("错题本", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${wrongQuestions.size} 题", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE74C3C))
                    }
                }
            }
        }

        // Category title
        item {
            Text(
                text = "📚 训练科目考场分类",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Categories list
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.startPractice(category) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KunerOrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (category.iconName) {
                                "sports_basketball" -> Icons.Default.SportsBasketball
                                "code" -> Icons.Default.Code
                                else -> Icons.Default.Psychology
                            },
                            contentDescription = "Category Icon",
                            tint = KunerOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp,
                            maxLines = 2
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.startPractice(category) }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "开始刷题",
                            tint = KunerOrange
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: PRACTICE SESSION ---
@Composable
fun PracticeScreen(viewModel: IkunViewModel) {
    val qList = viewModel.practiceQuestions
    val currentIndex = viewModel.currentPracticeIndex
    val categoryName = viewModel.selectedCategory?.name ?: "分类"

    if (qList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("该分类下暂无试题，正在为你排期...", color = Color.Gray)
        }
        return
    }

    val currentQ = qList[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Topic Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${categoryName} (${currentIndex + 1}/${qList.size})",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            Text(
                text = when (currentQ.type) {
                    "SINGLE" -> "单选题"
                    "MULTI" -> "多选题 (警告：多选)"
                    else -> "判断题"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = KunerCharcoal,
                modifier = Modifier
                    .background(KunerGrey.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question Details
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = currentQ.content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp
                )
            }

            // Interactive Answers
            items(currentQ.options) { option ->
                val optionCode = option.firstOrNull()?.toString() ?: ""
                val isSelected = viewModel.practiceSelectedAnswers.contains(optionCode)
                
                val borderAlpha = if (isSelected) 1f else 0.15f
                val cardColor = if (isSelected) KunerOrangeLight else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) KunerOrange else MaterialTheme.colorScheme.onSurface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !viewModel.isPracticeAnswered) {
                            viewModel.selectPracticeOption(optionCode, currentQ.type == "MULTI")
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = BorderStroke(1.dp, KunerOrange.copy(alpha = borderAlpha))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (!viewModel.isPracticeAnswered) {
                                    viewModel.selectPracticeOption(optionCode, currentQ.type == "MULTI")
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = KunerOrange),
                            enabled = !viewModel.isPracticeAnswered
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Results Explanation and AI Section
            if (viewModel.isPracticeAnswered) {
                item {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (viewModel.isPracticeAnswerCorrect) Color(0xFFE8F8F5) else Color(0xFFFCE4D6)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (viewModel.isPracticeAnswerCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = "Result Icon",
                                tint = if (viewModel.isPracticeAnswerCorrect) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (viewModel.isPracticeAnswerCorrect) "回答正确！鸡你太美！" else "回答错误，真爱粉丝需要更加油！",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.isPracticeAnswerCorrect) Color(0xFF16A085) else Color(0xFFC0392B)
                                )
                                Text(
                                    text = "正确答案: ${currentQ.correctAnswer}  |  您的回答: ${viewModel.practiceSelectedAnswers.sorted().joinToString(",")}",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                item {
                    // Local static explanation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💡 考点点拨", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KunerCharcoal)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // AI Expert Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF0E6)),
                        border = BorderStroke(1.dp, KunerOrange.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = KunerOrange
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI 巅峰讲解",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = KunerOrange
                                    )
                                }
                                
                                Button(
                                    onClick = { viewModel.requestAiExplanation(categoryName, currentQ, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = KunerOrange),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("一键询问AI", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when (val state = viewModel.aiState) {
                                is AiState.Idle -> {
                                    Text(
                                        text = "点击上面的一键询问，由 AI 导师（可自定义配置API）为您提供风趣透彻的 ikun 梗解析原理解析！",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        lineHeight = 16.sp
                                    )
                                }
                                is AiState.Loading -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = KunerOrange, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("AI 导师中分梳理中，正在极速响应...", fontSize = 11.sp, color = KunerOrange)
                                    }
                                }
                                is AiState.Success -> {
                                    Text(
                                        text = state.text,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)).padding(12.dp)
                                    )
                                }
                                is AiState.Error -> {
                                    Text(
                                        text = "⚠️ 请求异常:\n${state.message}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFC0392B),
                                        lineHeight = 16.sp,
                                        modifier = Modifier.background(Color(0xFFFDEDEC), RoundedCornerShape(8.dp)).padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submits / Next Question Controls
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!viewModel.isPracticeAnswered) {
                Button(
                    onClick = { viewModel.submitPracticeAnswer() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KunerOrange),
                    shape = RoundedCornerShape(10.dp),
                    enabled = viewModel.practiceSelectedAnswers.isNotEmpty()
                ) {
                    Text("提交答案 (排期评阅)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Button(
                    onClick = { viewModel.nextPracticeQuestion() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KunerCharcoal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    val label = if (currentIndex < qList.size - 1) "下一题" else "完成刷题"
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- SCREEN 3: MOCK EXAM SETUP ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockExamSetupScreen(viewModel: IkunViewModel) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "🏆 自定义机考模拟考试",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "自定义调节题型、题量和时间，全方位模拟真实竞技考场。",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
            }

            // 1. Question Type Selection
            item {
                Text("1. 选择考题类型 (多选)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.examTypeSingle,
                            onCheckedChange = { viewModel.examTypeSingle = it },
                            colors = CheckboxDefaults.colors(checkedColor = KunerOrange)
                        )
                        Text("单选", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.examTypeMulti,
                            onCheckedChange = { viewModel.examTypeMulti = it },
                            colors = CheckboxDefaults.colors(checkedColor = KunerOrange)
                        )
                        Text("多选", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.examTypeTf,
                            onCheckedChange = { viewModel.examTypeTf = it },
                            colors = CheckboxDefaults.colors(checkedColor = KunerOrange)
                        )
                        Text("判断", fontSize = 13.sp)
                    }
                }
                if (!viewModel.examTypeSingle && !viewModel.examTypeMulti && !viewModel.examTypeTf) {
                    Text("⚠️ 至少选择一种题型才可以开启考试哦！", color = Color(0xFFC0392B), fontSize = 11.sp)
                }
            }

            // 2. Question Count Selector
            item {
                Text("2. 设置本场考题数量", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val countOptions = listOf(3, 5, 10, 15, 20)
                    countOptions.forEach { count ->
                        val isSelected = viewModel.examQuestionCount == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) KunerOrange else KunerGrey.copy(alpha = 0.2f))
                                .clickable { viewModel.examQuestionCount = count },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count 题",
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 3. Exam Duration Selector
            item {
                Text("3. 设定考试时限 (分钟)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timeOptions = listOf(1, 5, 10, 15, 30)
                    timeOptions.forEach { mins ->
                        val isSelected = viewModel.examTimeMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) KunerOrange else KunerGrey.copy(alpha = 0.2f))
                                .clickable { viewModel.examTimeMinutes = mins },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$mins 分",
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Warnings & Launch Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.startMockExam() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KunerOrange),
                    enabled = (viewModel.examTypeSingle || viewModel.examTypeMulti || viewModel.examTypeTf)
                ) {
                    Icon(Icons.Default.SportsBasketball, contentDescription = "", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始模拟机考 (启动篮球排期)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- SCREEN 4: MOCK EXAM ACTIVE ---
@Composable
fun MockExamActiveScreen(viewModel: IkunViewModel) {
    val qList = viewModel.examQuestions
    val currentIndex = viewModel.currentExamIndex

    if (qList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("机考排队中，无可用试题...")
        }
        return
    }

    val currentQ = qList[currentIndex]
    val selectedAnswers = viewModel.examUserAnswers[currentQ.id] ?: emptySet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Active test header with ticking timer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = KunerCharcoal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("模拟考场绝密测试", color = Color.White, fontSize = 12.sp)
                    Text("进度: ${currentIndex + 1} / ${qList.size} 题", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                
                // Red ticking timer for tension!
                val secs = viewModel.examSecondsRemaining
                val minutes = secs / 60
                val seconds = secs % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                val isUrgent = secs <= 60

                Box(
                    modifier = Modifier
                        .background(if (isUrgent) Color(0xFFC0392B) else KunerOrange, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⏱️ $timeString",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question Details and layouts
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentQ.type) {
                            "SINGLE" -> "单选题"
                            "MULTI" -> "多选题"
                            else -> "判断题"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(KunerOrange, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentQ.content,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp
                )
            }

            // Selection buttons list
            items(currentQ.options) { option ->
                val optionCode = option.firstOrNull()?.toString() ?: ""
                val isSelected = selectedAnswers.contains(optionCode)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectExamOption(optionCode, currentQ) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) KunerOrangeLight else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (isSelected) KunerOrange else KunerGrey.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.selectExamOption(optionCode, currentQ) },
                            colors = CheckboxDefaults.colors(checkedColor = KunerOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, fontSize = 13.sp, color = if (isSelected) KunerOrange else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Previous, Next, and Submit controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (currentIndex > 0) {
                Button(
                    onClick = { viewModel.currentExamIndex-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KunerGrey),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("上一题", color = Color.DarkGray, fontSize = 13.sp)
                }
            }

            if (currentIndex < qList.size - 1) {
                Button(
                    onClick = { viewModel.currentExamIndex++ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KunerCharcoal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("下一题", color = Color.White, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = { viewModel.submitMockExam() },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("提交试卷", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// --- SCREEN 5: MOCK EXAM RESULTS ---
@Composable
fun MockExamResultScreen(viewModel: IkunViewModel) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Basketball scoring ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(KunerOrangeLight, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${viewModel.examScore}",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KunerOrange
                    )
                    Text("考场得分", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (viewModel.examScore >= 60) "🎉 通关！及格通关啦！" else "小黑子露出鸡脚了吧？不及格！",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (viewModel.examScore >= 60) Color(0xFF2ECC71) else Color(0xFFE74C3C)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (viewModel.examScore >= 60) {
                    "“鸡你太美，练习时长两年半的结课评价极佳。继续刷题积累，坤坤在宇宙尽头为你加油鼓劲！”"
                } else {
                    "“哦呦？中分头顶得不够顺溜，篮球卡点也不足！别气馁，多去重温 [错题本]，把遗失的坤分赢回来！”"
                },
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("本次机考总题量:", fontSize = 13.sp, color = Color.Gray)
                Text("${viewModel.examTotalCount} 道", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("顺利答准题数:", fontSize = 13.sp, color = Color.Gray)
                Text("${viewModel.examCorrectCount} 道", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2ECC71))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("答错录入错题本:", fontSize = 13.sp, color = Color.Gray)
                Text("${viewModel.examTotalCount - viewModel.examCorrectCount} 道", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE74C3C))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KunerCharcoal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("返回学堂大厅", color = Color.White)
            }
        }
    }
}

// --- SCREEN 6: WRONG QUESTIONS BOOK (错题本) ---
@Composable
fun ErrorBookScreen(viewModel: IkunViewModel) {
    val wqList by viewModel.wrongQuestions.collectAsStateWithLifecycle()

    if (wqList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsBasketball,
                contentDescription = "Empty",
                tint = KunerGrey,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "错题空空如也！",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "你真是最巅峰最顶级的 Kuner 练习生！所有科目百分百完全拿捏！快去首页开启模拟大考挑战吧！",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Dashboard header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDEDEC)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error icon",
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("错题坚守集中营", fontWeight = FontWeight.Bold, color = Color(0xFFC0392B), fontSize = 14.sp)
                        Text("当在错题重做中答对时，该题将自动从错题本消灭移出！", color = Color.DarkGray, fontSize = 11.sp)
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE74C3C), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${wqList.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Massive redos CTA
            Button(
                onClick = { viewModel.startRedo() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KunerOrange)
            ) {
                Icon(Icons.Default.Restore, contentDescription = "", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("进入错题集中重做攻坚", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lists out all specific wrong questions
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wqList) { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (q.type) {
                                        "SINGLE" -> "单选题"
                                        "MULTI" -> "多选题"
                                        else -> "判断题"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(KunerCharcoal, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                                Text(
                                    text = "移除这道题",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE74C3C),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        viewModel.removeWrongQuestion(q.id)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(q.content, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("正确答案是: ${q.correctAnswer}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: WRONG QUESTIONS REDO SESSION ---
@Composable
fun ErrorBookRedoScreen(viewModel: IkunViewModel) {
    val wqList by viewModel.wrongQuestions.collectAsStateWithLifecycle()
    val currentIndex = viewModel.currentRedoIndex

    if (wqList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("恭喜！错题已被完全毕业消灭！", fontWeight = FontWeight.Bold, color = Color(0xFF2ECC71))
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateTo(AppScreen.ERROR_BOOK) }) {
                    Text("返回错题本大厅")
                }
            }
        }
        return
    }

    // Edge check
    val finalIndex = minOf(currentIndex, wqList.size - 1)
    val currentQ = wqList[finalIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Topic Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "错题攻坚重做中 (进度: ${finalIndex + 1}/${wqList.size})",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC0392B),
                fontSize = 14.sp
            )
            Text(
                text = when (currentQ.type) {
                    "SINGLE" -> "单选题"
                    "MULTI" -> "多选题"
                    else -> "判断题"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = KunerCharcoal,
                modifier = Modifier
                    .background(KunerGrey.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question details and buttons
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = currentQ.content,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 22.sp
                )
            }

            // Options rows
            items(currentQ.options) { option ->
                val optionCode = option.firstOrNull()?.toString() ?: ""
                val isSelected = viewModel.redoSelectedAnswers.contains(optionCode)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !viewModel.isRedoAnswered) {
                            viewModel.selectRedoOption(optionCode, currentQ.type == "MULTI")
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) KunerOrangeLight else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (isSelected) KunerOrange else KunerGrey.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (!viewModel.isRedoAnswered) {
                                    viewModel.selectRedoOption(optionCode, currentQ.type == "MULTI")
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = KunerOrange),
                            enabled = !viewModel.isRedoAnswered
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, fontSize = 13.sp, color = if (isSelected) KunerOrange else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Results Explanation and AI Section
            if (viewModel.isRedoAnswered) {
                item {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (viewModel.isRedoAnswerCorrect) Color(0xFFE8F8F5) else Color(0xFFFCE4D6)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (viewModel.isRedoAnswerCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = "Result Icon",
                                tint = if (viewModel.isRedoAnswerCorrect) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (viewModel.isRedoAnswerCorrect) "回答正确！顺利从错题本除名毕业！" else "回答依旧错误，请继续驻留集中营反思哦！",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.isRedoAnswerCorrect) Color(0xFF16A085) else Color(0xFFC0392B)
                                )
                                Text(
                                    text = "正确答案是: ${currentQ.correctAnswer}",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💡 考点纠偏", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KunerCharcoal)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // AI Expert Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF0E6)),
                        border = BorderStroke(1.dp, KunerOrange.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = KunerOrange
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI 错题纠偏诊治",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = KunerOrange
                                    )
                                }
                                
                                Button(
                                    onClick = { viewModel.requestAiExplanation("错题集", currentQ, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = KunerOrange),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("一键询问AI", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when (val state = viewModel.redoAiState) {
                                is AiState.Idle -> {
                                    Text(
                                        text = "点击上面一键询问，AI 会教你如何梳理知识框架，完美纠纷毕业！",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        lineHeight = 16.sp
                                    )
                                }
                                is AiState.Loading -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = KunerOrange, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("AI 导师纠偏把脉中...", fontSize = 11.sp, color = KunerOrange)
                                    }
                                }
                                is AiState.Success -> {
                                    Text(
                                        text = state.text,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)).padding(10.dp)
                                    )
                                }
                                is AiState.Error -> {
                                    Text(
                                        text = "⚠️ 请求故障:\n${state.message}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFC0392B),
                                        lineHeight = 16.sp,
                                        modifier = Modifier.background(Color(0xFFFDEDEC), RoundedCornerShape(8.dp)).padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons controls
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!viewModel.isRedoAnswered) {
                Button(
                    onClick = { viewModel.submitRedoAnswer(wqList) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KunerOrange),
                    shape = RoundedCornerShape(8.dp),
                    enabled = viewModel.redoSelectedAnswers.isNotEmpty()
                ) {
                    Text("核对错题解答", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Button(
                    onClick = { viewModel.nextRedoQuestion(wqList) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KunerCharcoal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val label = if (finalIndex < wqList.size - 1) "下一题" else "结束重做攻坚"
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- SCREEN 8: SETTINGS & API CONFIGURATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: IkunViewModel) {
    var apiKey by remember { mutableStateOf(viewModel.apiKeySetting) }
    var baseUrl by remember { mutableStateOf(viewModel.baseUrlSetting) }
    var modelName by remember { mutableStateOf(viewModel.modelNameSetting) }

    var showToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "⚙️ 全能配置面板 (含AI解析)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "设置自定义大语言模型 API 接口，并管理本地的学习刷题进度。",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
        }

        // Section: Theme Mode Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KunerOrange.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Theme",
                            tint = KunerOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "🌓 应用主题设置 (明暗双色)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "极致定制视觉体验。支持明亮（舒适健康）和暗黑（炫酷护眼）两种专属精心设计的配色主题，也可以选择跟随您的系统自动按需调换。",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themeOptions = listOf(
                            Triple("SYSTEM", "跟随系统", Icons.Default.Settings),
                            Triple("LIGHT", "明（明亮）", Icons.Default.WbSunny),
                            Triple("DARK", "暗（暗黑）", Icons.Default.NightsStay)
                        )

                        themeOptions.forEach { (mode, label, icon) ->
                            val isSelected = viewModel.themeSetting == mode
                            val bgSelectedColor = if (isSelected) KunerOrangeLight else MaterialTheme.colorScheme.surface
                            val borderSelectedColor = if (isSelected) KunerOrange else KunerGrey.copy(alpha = 0.3f)
                            val tintColor = if (isSelected) KunerOrange else Color.Gray
                            val textStyleColor = if (isSelected) KunerOrange else MaterialTheme.colorScheme.onSurface

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.saveThemeSetting(mode) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = bgSelectedColor),
                                border = BorderStroke(1.dp, borderSelectedColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = tintColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = textStyleColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 1: API Configuration Setup
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, KunerOrange.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "💡 自定义 API 接口 (Gemini/OpenAI Pro)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = KunerCharcoal
                    )

                    // API Key Field with hidden visual
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Gemini API Key (API密钥)") },
                        placeholder = { Text("默认使用 AI Studio 的 Secrets Key") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KunerOrange,
                            unfocusedBorderColor = KunerGrey,
                            focusedLabelColor = KunerOrange
                        ),
                        singleLine = true
                    )

                    // Base URL config (useful for proxy environments)
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL (接口中转端点)") },
                        placeholder = { Text("例如 https://generativelanguage.googleapis.com/") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KunerOrange,
                            unfocusedBorderColor = KunerGrey,
                            focusedLabelColor = KunerOrange
                        ),
                        singleLine = true
                    )

                    // Model Name Field
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("Model Name (模型名称)") },
                        placeholder = { Text("默认使用 gemini-3.5-flash") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KunerOrange,
                            unfocusedBorderColor = KunerGrey,
                            focusedLabelColor = KunerOrange
                        ),
                        singleLine = true
                    )

                    // Save settings button
                    Button(
                        onClick = {
                            viewModel.saveApiSettings(apiKey, baseUrl, modelName)
                            showToast = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = KunerOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("保存接口配置", color = Color.White)
                    }

                    if (showToast) {
                        Text(
                            text = "✅ 接口信息配置已在存储中极速热重载更新！",
                            color = Color(0xFF2ECC71),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // Section 2: Instructions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KunerCharcoal),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📌 密钥配置帮助说明书", fontWeight = FontWeight.Bold, color = KunerOrange, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. AI Studio 在线预览会自动将用户 Secrets 的 GEMINI_API_KEY 注入到 BuildConfig 里。如果您未额外配置密钥，程序直接使用系统自带的默认通道运行。\n\n" +
                               "2. 如果通道请求速度缓慢或受到网络阻碍限制，支持自行中转反向代理，并在上面修改 [Base URL] 字段实现智能连接！",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section 3: Reset App Stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE74C3C).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ 数据危险管理中心",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFFC0392B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "重置刷题进度和错题信息，所有学堂答题记录、模拟考试报告单以及记录的错题本都将被永久粉碎消失。",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var countClicks by remember { mutableStateOf(0) }
                    
                    Button(
                        onClick = {
                            if (countClicks >= 1) {
                                viewModel.resetStats()
                                countClicks = 0
                            } else {
                                countClicks++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (countClicks > 0) Color(0xFFE74C3C) else Color(0xFFE5E7E9)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (countClicks > 0) "⚠️ 真的要粉碎清空？点击第二次确认" else "清空累计刷题数据 & 全力清空错题",
                            color = if (countClicks > 0) Color.White else Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
