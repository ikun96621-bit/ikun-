package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class IkunRepository(private val context: Context) {
    private val db = IkunDatabase.getDatabase(context)
    private val dao = db.ikunDao()

    val categories: Flow<List<Category>> = dao.getAllCategories()
    val wrongQuestions: Flow<List<Question>> = dao.getWrongQuestions()
    val allProgress: Flow<List<UserProgress>> = dao.getAllProgress()

    /**
     * Seeds initial questions and categories if the data isn't there already.
     */
    suspend fun seedIfNeeded() {
        IkunSeedData.categories.forEach {
            dao.insertCategory(it)
        }
        val allQuestions = dao.getAllQuestionsSync()
        if (allQuestions.isEmpty()) {
            IkunSeedData.questions.forEach {
                dao.insertQuestion(it)
            }
        }
    }

    fun getQuestionsByCategory(categoryId: Int): Flow<List<Question>> {
        return dao.getQuestionsByCategory(categoryId)
    }

    suspend fun getQuestionsByCategorySync(categoryId: Int): List<Question> {
        return dao.getQuestionsByCategorySync(categoryId)
    }

    suspend fun getAllQuestionsSync(): List<Question> {
        return dao.getAllQuestionsSync()
    }

    /**
     * Save progress. If correct, remove from wrong_questions (错题本).
     * If incorrect, add to wrong_questions.
     */
    suspend fun saveProgress(questionId: Int, isCorrect: Boolean, userAnswer: String) {
        val progress = UserProgress(
            questionId = questionId,
            isCorrect = isCorrect,
            userAnswer = userAnswer,
            answeredTimestamp = System.currentTimeMillis()
        )
        dao.insertProgress(progress)

        if (isCorrect) {
            dao.deleteWrongQuestion(questionId)
        } else {
            val wrongQ = WrongQuestion(
                questionId = questionId,
                addedTimestamp = System.currentTimeMillis()
            )
            dao.insertWrongQuestion(wrongQ)
        }
    }

    /**
     * Skip or manually remove a wrong question from error book.
     */
    suspend fun removeWrongQuestion(questionId: Int) {
        dao.deleteWrongQuestion(questionId)
    }

    /**
     * Clear statistics and progress, including error book.
     */
    suspend fun resetAllData() {
        dao.clearProgress()
        dao.clearWrongQuestions()
    }
}
