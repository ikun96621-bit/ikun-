package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Collections

// Navigation / Screen selection
enum class AppScreen {
    DASHBOARD,
    PRACTICE,
    MOCK_EXAM_SETUP,
    MOCK_EXAM_ACTIVE,
    MOCK_EXAM_RESULT,
    ERROR_BOOK,
    ERROR_BOOK_REDO,
    SETTINGS
}

// AI explanation state
sealed interface AiState {
    object Idle : AiState
    object Loading : AiState
    data class Success(val text: String) : AiState
    data class Error(val message: String) : AiState
}

class IkunViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IkunRepository(application)
    private val context = application.applicationContext

    // SharedPreferences for API configuration
    private val prefs = context.getSharedPreferences("ikun_prefs", Context.MODE_PRIVATE)

    // Flow lists
    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wrongQuestions: StateFlow<List<Question>> = repository.wrongQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progress: StateFlow<List<UserProgress>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI Navigation State ---
    var currentScreen by mutableStateOf(AppScreen.DASHBOARD)
        private set

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        if (screen == AppScreen.DASHBOARD) {
            resetPractice()
        }
    }

    // --- AI Configuration Settings ---
    var apiKeySetting by mutableStateOf(prefs.getString("api_key", "") ?: "")
    var baseUrlSetting by mutableStateOf(prefs.getString("base_url", "https://generativelanguage.googleapis.com/") ?: "")
    var modelNameSetting by mutableStateOf(prefs.getString("model_name", "gemini-3.5-flash") ?: "")

    fun saveApiSettings(key: String, url: String, model: String) {
        apiKeySetting = key
        baseUrlSetting = url
        modelNameSetting = model
        prefs.edit()
            .putString("api_key", key)
            .putString("base_url", url)
            .putString("model_name", model)
            .apply()
    }

    // --- Application Theme Management ---
    // Supported modes: "SYSTEM", "LIGHT", "DARK"
    var themeSetting by mutableStateOf(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
        private set

    fun saveThemeSetting(mode: String) {
        themeSetting = mode
        prefs.edit()
            .putString("theme_mode", mode)
            .apply()
    }

    // --- Seeding & Daily Goal ---
    var dailyGoalTarget by mutableStateOf(prefs.getInt("daily_goal_target", 10))
        private set

    var dailyCompletedCount by mutableStateOf(0)
        private set

    fun saveDailyGoalTarget(target: Int) {
        dailyGoalTarget = target
        prefs.edit().putInt("daily_goal_target", target).apply()
    }

    private fun getCurrentDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    fun incrementDailyAnswers(byCount: Int = 1) {
        val currentDateStr = getCurrentDateString()
        val savedDate = prefs.getString("daily_completed_date", "") ?: ""
        var currentCount = 0
        if (savedDate == currentDateStr) {
            currentCount = prefs.getInt("daily_completed_count", 0)
        }
        val newCount = currentCount + byCount
        prefs.edit()
            .putString("daily_completed_date", currentDateStr)
            .putInt("daily_completed_count", newCount)
            .apply()
        dailyCompletedCount = newCount
    }

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
        val currentDateStr = getCurrentDateString()
        val savedDate = prefs.getString("daily_completed_date", "") ?: ""
        if (savedDate == currentDateStr) {
            dailyCompletedCount = prefs.getInt("daily_completed_count", 0)
        } else {
            dailyCompletedCount = 0
        }
    }

    // --- NORMAL PRACTICE STATE ---
    var selectedCategory by mutableStateOf<Category?>(null)
    var practiceQuestions by mutableStateOf<List<Question>>(emptyList())
    var currentPracticeIndex by mutableStateOf(0)
    var practiceSelectedAnswers by mutableStateOf<Set<String>>(emptySet()) // For MULTI, can hold multiple options like "A", "B" etc.
    var isPracticeAnswered by mutableStateOf(false)
    var isPracticeAnswerCorrect by mutableStateOf(false)
    var showPracticeExplanation by mutableStateOf(false)
    
    // AI query state
    var aiState by mutableStateOf<AiState>(AiState.Idle)
        private set

    fun startPractice(category: Category) {
        selectedCategory = category
        viewModelScope.launch {
            repository.getQuestionsByCategorySync(category.id).let {
                practiceQuestions = it
                currentPracticeIndex = 0
                resetQuestionState()
                navigateTo(AppScreen.PRACTICE)
            }
        }
    }

    fun startRandomPractice() {
        viewModelScope.launch {
            val allQs = repository.getAllQuestionsSync().shuffled()
            selectedCategory = Category(-1, "随机全能刷题", "包含全部科目题库中的所有试题进行完全随机乱序练习。", "shuffle")
            practiceQuestions = allQs
            currentPracticeIndex = 0
            resetQuestionState()
            navigateTo(AppScreen.PRACTICE)
        }
    }

    private fun resetQuestionState() {
        practiceSelectedAnswers = emptySet()
        isPracticeAnswered = false
        isPracticeAnswerCorrect = false
        showPracticeExplanation = false
        aiState = AiState.Idle
    }

    fun selectPracticeOption(optionCode: String, isMulti: Boolean) {
        if (isPracticeAnswered) return
        if (isMulti) {
            practiceSelectedAnswers = if (practiceSelectedAnswers.contains(optionCode)) {
                practiceSelectedAnswers - optionCode
            } else {
                practiceSelectedAnswers + optionCode
            }
        } else {
            practiceSelectedAnswers = setOf(optionCode)
        }
    }

    fun submitPracticeAnswer() {
        if (practiceQuestions.isEmpty() || isPracticeAnswered) return
        val currentQ = practiceQuestions[currentPracticeIndex]
        
        val userAnswerString = practiceSelectedAnswers.sorted().joinToString(",")
        val isCorrect = evaluateAnswer(currentQ, userAnswerString)

        isPracticeAnswered = true
        isPracticeAnswerCorrect = isCorrect
        showPracticeExplanation = true

        viewModelScope.launch {
            repository.saveProgress(currentQ.id, isCorrect, userAnswerString)
        }
        incrementDailyAnswers(1)
    }

    fun nextPracticeQuestion() {
        if (currentPracticeIndex < practiceQuestions.size - 1) {
            currentPracticeIndex++
            resetQuestionState()
        } else {
            // Completed all questions in category
            navigateTo(AppScreen.DASHBOARD)
        }
    }

    private fun resetPractice() {
        selectedCategory = null
        practiceQuestions = emptyList()
        currentPracticeIndex = 0
        resetQuestionState()
    }

    // --- MOCK EXAM STATE ---
    // Customization variables and backups for dual-compatibility
    var examTimeMinutesInput by mutableStateOf("10")
    var examSingleCountInput by mutableStateOf("3")
    var examMultiCountInput by mutableStateOf("2")
    var examTfCountInput by mutableStateOf("2")

    val examTimeMinutes: Int
        get() = examTimeMinutesInput.toIntOrNull()?.coerceIn(1, 1440) ?: 10

    val examSingleCount: Int
        get() = examSingleCountInput.toIntOrNull()?.coerceAtLeast(0) ?: 0

    val examMultiCount: Int
        get() = examMultiCountInput.toIntOrNull()?.coerceAtLeast(0) ?: 0

    val examTfCount: Int
        get() = examTfCountInput.toIntOrNull()?.coerceAtLeast(0) ?: 0

    // Running exam variables
    var examQuestions by mutableStateOf<List<Question>>(emptyList())
    var currentExamIndex by mutableStateOf(0)
    // Map of questionId -> answers chosen
    var examUserAnswers by mutableStateOf<Map<Int, Set<String>>>(emptyMap())
    
    // Timer variables
    var examSecondsRemaining by mutableStateOf(0)
    var isExamRunning by mutableStateOf(false)
    private var timerJob: Job? = null

    // Results variables
    var examScore by mutableStateOf(0)
    var examCorrectCount by mutableStateOf(0)
    var examTotalCount by mutableStateOf(0)

    fun startMockExam() {
        viewModelScope.launch {
            val allQs = repository.getAllQuestionsSync()
            
            val singleTarget = examSingleCount
            val multiTarget = examMultiCount
            val tfTarget = examTfCount
            val durationMin = examTimeMinutes

            // Select specified count for each type
            val singleQs = allQs.filter { it.type == "SINGLE" }.shuffled().take(singleTarget)
            val multiQs = allQs.filter { it.type == "MULTI" }.shuffled().take(multiTarget)
            val tfQs = allQs.filter { it.type == "TF" }.shuffled().take(tfTarget)

            val combined = (singleQs + multiQs + tfQs).shuffled()
            val finalCount = combined.size
            
            examQuestions = combined
            currentExamIndex = 0
            examUserAnswers = emptyMap()
            examSecondsRemaining = durationMin * 60
            examTotalCount = finalCount
            
            if (finalCount > 0) {
                isExamRunning = true
                navigateTo(AppScreen.MOCK_EXAM_ACTIVE)
                startTimer()
            } else {
                // No questions matched
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (examSecondsRemaining > 0 && isExamRunning) {
                delay(1000)
                examSecondsRemaining--
            }
            if (examSecondsRemaining <= 0 && isExamRunning) {
                // Time's up, auto submit
                submitMockExam()
            }
        }
    }

    fun selectExamOption(optionCode: String, question: Question) {
        if (!isExamRunning) return
        val currentAnswers = examUserAnswers[question.id] ?: emptySet()
        val newAnswers = if (question.type == "MULTI") {
            if (currentAnswers.contains(optionCode)) {
                currentAnswers - optionCode
            } else {
                currentAnswers + optionCode
            }
        } else {
            setOf(optionCode)
        }
        examUserAnswers = examUserAnswers + (question.id to newAnswers)
    }

    fun submitMockExam() {
        isExamRunning = false
        timerJob?.cancel()

        // Grade exam & save progress
        var correctCount = 0
        viewModelScope.launch {
            examQuestions.forEach { q ->
                val userAnswers = examUserAnswers[q.id] ?: emptySet()
                val ansStr = userAnswers.sorted().joinToString(",")
                val isCorrect = evaluateAnswer(q, ansStr)
                if (isCorrect) {
                    correctCount++
                }
                repository.saveProgress(q.id, isCorrect, ansStr)
            }
            examCorrectCount = correctCount
            examScore = if (examTotalCount > 0) (correctCount * 100) / examTotalCount else 0
            incrementDailyAnswers(examQuestions.size)
            navigateTo(AppScreen.MOCK_EXAM_RESULT)
        }
    }

    // --- WRONG QUESTIONS REDO STATE ---
    var currentRedoIndex by mutableStateOf(0)
    var redoSelectedAnswers by mutableStateOf<Set<String>>(emptySet())
    var isRedoAnswered by mutableStateOf(false)
    var isRedoAnswerCorrect by mutableStateOf(false)
    var showRedoExplanation by mutableStateOf(false)
    var redoAiState by mutableStateOf<AiState>(AiState.Idle)

    fun startRedo() {
        currentRedoIndex = 0
        resetRedoState()
        navigateTo(AppScreen.ERROR_BOOK_REDO)
    }

    private fun resetRedoState() {
        redoSelectedAnswers = emptySet()
        isRedoAnswered = false
        isRedoAnswerCorrect = false
        showRedoExplanation = false
        redoAiState = AiState.Idle
    }

    fun selectRedoOption(optionCode: String, isMulti: Boolean) {
        if (isRedoAnswered) return
        if (isMulti) {
            redoSelectedAnswers = if (redoSelectedAnswers.contains(optionCode)) {
                redoSelectedAnswers - optionCode
            } else {
                redoSelectedAnswers + optionCode
            }
        } else {
            redoSelectedAnswers = setOf(optionCode)
        }
    }

    fun submitRedoAnswer(wqList: List<Question>) {
        if (wqList.isEmpty() || isRedoAnswered) return
        val currentQ = wqList[currentRedoIndex]
        
        val userAnswerString = redoSelectedAnswers.sorted().joinToString(",")
        val isCorrect = evaluateAnswer(currentQ, userAnswerString)

        isRedoAnswered = true
        isRedoAnswerCorrect = isCorrect
        showRedoExplanation = true

        viewModelScope.launch {
            // Save progress. If correct, Repository will automatically delete it from wrong_questions!
            repository.saveProgress(currentQ.id, isCorrect, userAnswerString)
        }
        incrementDailyAnswers(1)
    }

    fun nextRedoQuestion(wqList: List<Question>) {
        if (currentRedoIndex < wqList.size - 1) {
            // We stay on the index or increment depending on database deletions.
            // Since it is deleted from the table when correct, the list size shrinks!
            // If correct, the item is removed, so the NEXT item shifts to currentRedoIndex!
            // If incorrect, the item remains, so we increment the index to go to next.
            if (isRedoAnswerCorrect) {
                // Do not increment currentRedoIndex unless it was the very last item
                if (currentRedoIndex >= wqList.size - 1) {
                    currentRedoIndex = maxOf(0, currentRedoIndex - 1)
                }
            } else {
                currentRedoIndex++
            }
            resetRedoState()
        } else {
            // Reached the end
            navigateTo(AppScreen.ERROR_BOOK)
        }
    }

    // --- AI ANALYSIS INTERACTION ---
    fun requestAiExplanation(categoryName: String, question: Question, isRedoMode: Boolean) {
        viewModelScope.launch {
            if (isRedoMode) {
                redoAiState = AiState.Loading
                val userAns = redoSelectedAnswers.sorted().joinToString(",")
                val result = AiService.fetchExplanation(context, categoryName, question, userAns)
                redoAiState = result.fold(
                    onSuccess = { AiState.Success(it) },
                    onFailure = { AiState.Error(it.message ?: "未知错误") }
                )
            } else {
                aiState = AiState.Loading
                val userAns = practiceSelectedAnswers.sorted().joinToString(",")
                val result = AiService.fetchExplanation(context, categoryName, question, userAns)
                aiState = result.fold(
                    onSuccess = { AiState.Success(it) },
                    onFailure = { AiState.Error(it.message ?: "未知错误") }
                )
            }
        }
    }

    // --- STATISTICS UTILS ---
    fun removeWrongQuestion(questionId: Int) {
        viewModelScope.launch {
            repository.removeWrongQuestion(questionId)
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    // --- INNER HELPER ---
    private fun evaluateAnswer(q: Question, userAnswer: String): Boolean {
        // Equal evaluation of answer (e.g. "A" vs "A", "A,B" vs "A,B", "True" vs "True")
        // For TF, we can support direct string map (if options are Correct/Incorrect etc.)
        // Options: A, B
        val mappedUserAns = if (q.type == "TF") {
            // T/F: CorrectAnswer can be "A" or "B". User answer is "A" (option select) or "B". Compared directly.
            userAnswer
        } else {
            userAnswer
        }
        return q.correctAnswer.trim().equals(mappedUserAns.trim(), ignoreCase = true)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
