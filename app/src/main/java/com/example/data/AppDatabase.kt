package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ---------------- ENTITIES ----------------

@Entity(tableName = "generated_contents")
data class GeneratedContent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "ppt", "quiz", "worksheet", "remedial", "activity"
    val stage: String, // "ابتدائي", "متوسط", "ثانوي"
    val subject: String,
    val contentJson: String, // JSON presentation structure, worksheets, or tests
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val stage: String,
    val grade: String,
    val notes: String = "",
    val score: Double = 0.0 // average score for result analysis
)

@Entity(tableName = "teacher_logs")
data class TeacherLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "course" (دورة), "initiative" (مبادرة), "achievement" (إنجاز), "performance" (أداء وظيفي)
    val date: String,
    val description: String,
    val score: Int = 100 // Job rating or self assessment out of 100
)

// ---------------- DAOS ----------------

@Dao
interface EducationDao {
    // Generated Contents
    @Query("SELECT * FROM generated_contents ORDER BY timestamp DESC")
    fun getAllContents(): Flow<List<GeneratedContent>>

    @Query("SELECT * FROM generated_contents WHERE type = :type ORDER BY timestamp DESC")
    fun getContentsByType(type: String): Flow<List<GeneratedContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: GeneratedContent)

    @Delete
    suspend fun deleteContent(content: GeneratedContent)

    @Query("DELETE FROM generated_contents WHERE id = :id")
    suspend fun deleteContentById(id: Int)

    // Students
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Int)

    // Teacher Logs
    @Query("SELECT * FROM teacher_logs ORDER BY date DESC")
    fun getAllTeacherLogs(): Flow<List<TeacherLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherLog(log: TeacherLog)

    @Delete
    suspend fun deleteTeacherLog(log: TeacherLog)

    @Query("DELETE FROM teacher_logs WHERE id = :id")
    suspend fun deleteTeacherLogById(id: Int)
}

// ---------------- DATABASE ----------------

@Database(entities = [GeneratedContent::class, Student::class, TeacherLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun educationDao(): EducationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_teacher_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ---------------- REPOSITORY ----------------

class EducationRepository(private val dao: EducationDao) {
    val allContents: Flow<List<GeneratedContent>> = dao.getAllContents()
    fun getContentsByType(type: String) = dao.getContentsByType(type)
    suspend fun insertContent(content: GeneratedContent) = dao.insertContent(content)
    suspend fun deleteContentById(id: Int) = dao.deleteContentById(id)

    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    suspend fun insertStudent(student: Student) = dao.insertStudent(student)
    suspend fun deleteStudentById(id: Int) = dao.deleteStudentById(id)

    val allTeacherLogs: Flow<List<TeacherLog>> = dao.getAllTeacherLogs()
    suspend fun insertTeacherLog(log: TeacherLog) = dao.insertTeacherLog(log)
    suspend fun deleteTeacherLogById(id: Int) = dao.deleteTeacherLogById(id)
}
