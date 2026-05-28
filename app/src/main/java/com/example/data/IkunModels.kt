package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val iconName: String
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val type: String, // "SINGLE", "MULTI", "TF"
    val content: String,
    val options: List<String>, // List of options, converted to/from JSON
    val correctAnswer: String, // E.g., "A", "A,B", "True", "False"
    val explanation: String // Local static explanation
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val questionId: Int,
    val isCorrect: Boolean,
    val userAnswer: String,
    val answeredTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wrong_questions")
data class WrongQuestion(
    @PrimaryKey val questionId: Int,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isRetried: Boolean = false
)
