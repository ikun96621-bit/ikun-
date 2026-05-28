package com.example.data

import android.content.Context
import androidx.room.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { adapter.fromJson(it) }
    }
}

@Dao
interface IkunDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Query("SELECT * FROM questions WHERE categoryId = :categoryId")
    fun getQuestionsByCategory(categoryId: Int): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE categoryId = :categoryId")
    suspend fun getQuestionsByCategorySync(categoryId: Int): List<Question>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestionsSync(): List<Question>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionByIdSync(id: Int): Question?

    // Wrong questions (Error Book) joining
    @Query("""
        SELECT q.* FROM questions q 
        INNER JOIN wrong_questions w ON q.id = w.questionId 
        ORDER BY w.addedTimestamp DESC
    """)
    fun getWrongQuestions(): Flow<List<Question>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongQuestion(wrongQuestion: WrongQuestion)

    @Query("DELETE FROM wrong_questions WHERE questionId = :questionId")
    suspend fun deleteWrongQuestion(questionId: Int)

    // User progress queries
    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(userProgress: UserProgress)

    @Query("DELETE FROM user_progress")
    suspend fun clearProgress()

    @Query("DELETE FROM wrong_questions")
    suspend fun clearWrongQuestions()
}

@Database(
    entities = [Category::class, Question::class, UserProgress::class, WrongQuestion::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class IkunDatabase : RoomDatabase() {
    abstract fun ikunDao(): IkunDao

    companion object {
        @Volatile
        private var INSTANCE: IkunDatabase? = null

        fun getDatabase(context: Context): IkunDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IkunDatabase::class.java,
                    "ikun_shuati_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
