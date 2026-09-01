package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LearningAnalysis(
    val subject: String,
    val topic: String,
    val keyConcepts: List<String>,
    val difficulty: String,
    val shortSummary: String,
    val studentGoal: String
)

@JsonClass(generateAdapter = true)
data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

@JsonClass(generateAdapter = true)
data class LearningGap(
    val score: Int,
    val total: Int,
    val conceptsUnderstood: List<String>,
    val conceptsToImprove: List<String>,
    val weakestConcept: String,
    val gapMessage: String
)

@JsonClass(generateAdapter = true)
data class ProgressProfile(
    val topicsStudied: Int,
    val averageScorePercent: Int,
    val strongConcepts: List<String>,
    val weakConcepts: List<String>,
    val recentSessions: List<LearningSession>
)

@JsonClass(generateAdapter = true)
data class LearningSession(
    val topic: String,
    val score: Int,
    val date: String
)
