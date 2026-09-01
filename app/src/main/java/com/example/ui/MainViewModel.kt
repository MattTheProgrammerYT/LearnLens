package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIEngine
import com.example.ai.GeminiAIEngine
import com.example.model.LearningAnalysis
import com.example.model.LearningGap
import com.example.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class AnalysisSuccess(val analysis: LearningAnalysis) : UiState()
    data class QuizReady(val questions: List<QuizQuestion>) : UiState()
    data class GapAnalysisSuccess(val gap: LearningGap) : UiState()
    data class FollowUpQuestionReady(val question: QuizQuestion) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {
    private val aiEngine: AIEngine = GeminiAIEngine()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _currentMaterial = MutableStateFlow<String>("")
    val currentMaterial = _currentMaterial.asStateFlow()

    private val _userAnswers = MutableStateFlow<MutableList<String>>(mutableListOf())
    val userAnswers = _userAnswers.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    fun resetState() {
        _uiState.value = UiState.Idle
        _currentMaterial.value = ""
        _userAnswers.value = mutableListOf()
        _quizScore.value = 0
    }

    fun analyzeSampleMaterial() {
        val sample = "Photosynthesis is the process by which plants use sunlight, water, and carbon dioxide to create oxygen and energy in the form of sugar. The process takes place in the chloroplasts, using chlorophyll."
        analyzeMaterial(sample)
    }

    fun analyzeMaterial(material: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _currentMaterial.value = material
            try {
                val analysis = aiEngine.analyzeLearningMaterial(material)
                _uiState.value = UiState.AnalysisSuccess(analysis)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun generateQuiz(topic: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val questions = aiEngine.generateQuiz(topic, _currentMaterial.value)
                _userAnswers.value = mutableListOf()
                _quizScore.value = 0
                _uiState.value = UiState.QuizReady(questions)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to generate quiz")
            }
        }
    }

    fun recordAnswer(answer: String, isCorrect: Boolean) {
        val currentAnswers = _userAnswers.value.toMutableList()
        currentAnswers.add(answer)
        _userAnswers.value = currentAnswers
        if (isCorrect) {
            _quizScore.value += 1
        }
    }

    fun finishQuiz(topic: String, total: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val gap = aiEngine.analyzeQuizResults(
                    topic = topic,
                    score = _quizScore.value,
                    total = total,
                    material = _currentMaterial.value,
                    userAnswers = _userAnswers.value
                )
                _uiState.value = UiState.GapAnalysisSuccess(gap)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to analyze learning gap")
            }
        }
    }

    fun generateFollowUp(weakConcept: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val question = aiEngine.generateFollowUpQuestion(weakConcept)
                _uiState.value = UiState.FollowUpQuestionReady(question)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to generate follow up")
            }
        }
    }
}
