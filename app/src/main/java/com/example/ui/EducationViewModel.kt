package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EducationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EducationRepository

    val allContents: StateFlow<List<GeneratedContent>>
    val allStudents: StateFlow<List<Student>>
    val allTeacherLogs: StateFlow<List<TeacherLog>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EducationRepository(database.educationDao())

        allContents = repository.allContents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allStudents = repository.allStudents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allTeacherLogs = repository.allTeacherLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Insert some default Saudi students if database is empty to populate the Analytics screen immediately!
        viewModelScope.launch {
            repository.allStudents.first().let { list ->
                if (list.isEmpty()) {
                    repository.insertStudent(Student(name = "أحمد الشهري", stage = "ابتدائي", grade = "الصف الخامس", notes = "متفوق، يحتاج أنشطة إثرائية", score = 95.0))
                    repository.insertStudent(Student(name = "سارة القحطاني", stage = "ابتدائي", grade = "الصف الخامس", notes = "تحتاج خطة علاجية في مهارات الكسور", score = 64.0))
                    repository.insertStudent(Student(name = "محمد الدوسري", stage = "ابتدائي", grade = "الصف الخامس", notes = "أداء متوسط ومشارك بالفصل", score = 82.0))
                    repository.insertStudent(Student(name = "فاطمة الغامدي", stage = "ابتدائي", grade = "الصف الخامس", notes = "متميزة في الرياضيات والعلوم", score = 98.0))
                    repository.insertStudent(Student(name = "عبدالرحمن الحربي", stage = "ابتدائي", grade = "الصف الخامس", notes = "يحتاج متابعة مستمرة لتحسين الإملاء", score = 55.0))
                }
            }
            repository.allTeacherLogs.first().let { list ->
                if (list.isEmpty()) {
                    repository.insertTeacherLog(TeacherLog(title = "دورة منصة مدرستي والذكاء الاصطناعي", type = "course", date = "2026-07-15", description = "حضور دورة تدريبية مكثفة حول توظيف الذكاء الاصطناعي التوليدي في منصة مدرستي لزيادة تفاعل الطلاب.", score = 95))
                    repository.insertTeacherLog(TeacherLog(title = "مبادرة المدرسة الرقمية المتميزة", type = "initiative", date = "2026-07-10", description = "تفعيل سجلات أعمال السنة الرقمية بنجاح لجميع فصول المرحلة الابتدائية.", score = 100))
                }
            }
        }
    }

    // UI States
    var topicInput by mutableStateOf("")
    var selectedStage by mutableStateOf("ابتدائي") // "ابتدائي", "متوسط", "ثانوي"
    var selectedSubject by mutableStateOf("الرياضيات") // "الرياضيات", "العلوم", "اللغة العربية", "الدراسات الإسلامية", "اللغة الإنجليزية"
    
    var isGenerating by mutableStateOf(false)
    var errorMsg by mutableStateOf<String?>(null)

    // Current Active generated detail
    var activeGeneratedTitle by mutableStateOf("")
    var activeGeneratedType by mutableStateOf("")
    var activeGeneratedContentJson by mutableStateOf("")

    // Student Dialog Forms
    var studentNameInput by mutableStateOf("")
    var studentStageInput by mutableStateOf("ابتدائي")
    var studentGradeInput by mutableStateOf("الصف الأول")
    var studentScoreInput by mutableStateOf("80")
    var studentNotesInput by mutableStateOf("")

    // Teacher Log Dialog Forms
    var logTitleInput by mutableStateOf("")
    var logTypeInput by mutableStateOf("achievement") // "course", "initiative", "achievement"
    var logDateInput by mutableStateOf("2026-07-20")
    var logDescInput by mutableStateOf("")
    var logScoreInput by mutableStateOf("95")

    fun generatePPT() {
        if (topicInput.isBlank()) return
        isGenerating = true
        errorMsg = null
        viewModelScope.launch {
            try {
                val result = GeminiService.generatePPT(topicInput, selectedStage, selectedSubject)
                activeGeneratedTitle = "عرض بوربوينت: $topicInput"
                activeGeneratedType = "ppt"
                activeGeneratedContentJson = result
                
                // Automatically save to local history
                repository.insertContent(
                    GeneratedContent(
                        title = activeGeneratedTitle,
                        type = "ppt",
                        stage = selectedStage,
                        subject = selectedSubject,
                        contentJson = result
                    )
                )
            } catch (e: Exception) {
                Log.e("EducationVM", "Error generating PPT", e)
                errorMsg = e.localizedMessage ?: "حدث خطأ غير متوقع"
            } finally {
                isGenerating = false
            }
        }
    }

    fun generateQuiz(count: Int = 5) {
        if (topicInput.isBlank()) return
        isGenerating = true
        errorMsg = null
        viewModelScope.launch {
            try {
                val result = GeminiService.generateQuiz(topicInput, selectedStage, selectedSubject, count)
                activeGeneratedTitle = "اختبار: $topicInput"
                activeGeneratedType = "quiz"
                activeGeneratedContentJson = result
                
                repository.insertContent(
                    GeneratedContent(
                        title = activeGeneratedTitle,
                        type = "quiz",
                        stage = selectedStage,
                        subject = selectedSubject,
                        contentJson = result
                    )
                )
            } catch (e: Exception) {
                Log.e("EducationVM", "Error generating Quiz", e)
                errorMsg = e.localizedMessage ?: "حدث خطأ غير متوقع"
            } finally {
                isGenerating = false
            }
        }
    }

    fun generateWorksheet() {
        if (topicInput.isBlank()) return
        isGenerating = true
        errorMsg = null
        viewModelScope.launch {
            try {
                val result = GeminiService.generateWorksheet(topicInput, selectedStage, selectedSubject)
                activeGeneratedTitle = "ورقة عمل: $topicInput"
                activeGeneratedType = "worksheet"
                activeGeneratedContentJson = result
                
                repository.insertContent(
                    GeneratedContent(
                        title = activeGeneratedTitle,
                        type = "worksheet",
                        stage = selectedStage,
                        subject = selectedSubject,
                        contentJson = result
                    )
                )
            } catch (e: Exception) {
                Log.e("EducationVM", "Error generating Worksheet", e)
                errorMsg = e.localizedMessage ?: "حدث خطأ غير متوقع"
            } finally {
                isGenerating = false
            }
        }
    }

    fun generatePlan(isRemedial: Boolean) {
        if (topicInput.isBlank()) return
        isGenerating = true
        errorMsg = null
        viewModelScope.launch {
            try {
                val result = GeminiService.generatePlan(topicInput, selectedStage, selectedSubject, isRemedial)
                val pType = if (isRemedial) "remedial" else "enrichment"
                activeGeneratedTitle = if (isRemedial) "خطة علاجية: $topicInput" else "خطة إثرائية: $topicInput"
                activeGeneratedType = pType
                activeGeneratedContentJson = result
                
                repository.insertContent(
                    GeneratedContent(
                        title = activeGeneratedTitle,
                        type = pType,
                        stage = selectedStage,
                        subject = selectedSubject,
                        contentJson = result
                    )
                )
            } catch (e: Exception) {
                Log.e("EducationVM", "Error generating Plan", e)
                errorMsg = e.localizedMessage ?: "حدث خطأ غير متوقع"
            } finally {
                isGenerating = false
            }
        }
    }

    fun generateActivityPlan() {
        if (topicInput.isBlank()) return
        isGenerating = true
        errorMsg = null
        viewModelScope.launch {
            try {
                val result = GeminiService.generateActivityPlan(topicInput, selectedStage, selectedSubject)
                activeGeneratedTitle = "خطة نشاط: $topicInput"
                activeGeneratedType = "activity"
                activeGeneratedContentJson = result
                
                repository.insertContent(
                    GeneratedContent(
                        title = activeGeneratedTitle,
                        type = "activity",
                        stage = selectedStage,
                        subject = selectedSubject,
                        contentJson = result
                    )
                )
            } catch (e: Exception) {
                Log.e("EducationVM", "Error generating Activity", e)
                errorMsg = e.localizedMessage ?: "حدث خطأ غير متوقع"
            } finally {
                isGenerating = false
            }
        }
    }

    fun saveStudent() {
        if (studentNameInput.isBlank()) return
        viewModelScope.launch {
            val scoreDouble = studentScoreInput.toDoubleOrNull() ?: 80.0
            repository.insertStudent(
                Student(
                    name = studentNameInput,
                    stage = studentStageInput,
                    grade = studentGradeInput,
                    notes = studentNotesInput,
                    score = scoreDouble
                )
            )
            // Clear fields
            studentNameInput = ""
            studentNotesInput = ""
            studentScoreInput = "80"
        }
    }

    fun deleteStudent(id: Int) {
        viewModelScope.launch {
            repository.deleteStudentById(id)
        }
    }

    fun saveTeacherLog() {
        if (logTitleInput.isBlank()) return
        viewModelScope.launch {
            val scoreInt = logScoreInput.toIntOrNull() ?: 95
            repository.insertTeacherLog(
                TeacherLog(
                    title = logTitleInput,
                    type = logTypeInput,
                    date = logDateInput,
                    description = logDescInput,
                    score = scoreInt
                )
            )
            // Clear fields
            logTitleInput = ""
            logDescInput = ""
            logScoreInput = "95"
        }
    }

    fun deleteTeacherLog(id: Int) {
        viewModelScope.launch {
            repository.deleteTeacherLogById(id)
        }
    }

    fun deleteContent(id: Int) {
        viewModelScope.launch {
            repository.deleteContentById(id)
        }
    }
}
