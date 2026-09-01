package com.example.ai

import com.example.model.LearningAnalysis
import com.example.model.LearningGap
import com.example.model.QuizQuestion

interface AIEngine {
    suspend fun analyzeLearningMaterial(input: String): LearningAnalysis
    suspend fun generateQuiz(topic: String, material: String): List<QuizQuestion>
    suspend fun analyzeQuizResults(topic: String, score: Int, total: Int, material: String, userAnswers: List<String>): LearningGap
    suspend fun generateFollowUpQuestion(weakConcept: String): QuizQuestion
}
