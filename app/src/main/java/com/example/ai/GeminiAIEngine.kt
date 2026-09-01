package com.example.ai

import com.example.BuildConfig
import com.example.model.LearningAnalysis
import com.example.model.LearningGap
import com.example.model.QuizQuestion
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.squareup.moshi.JsonClass

// -- Moshi classes for Gemini API --

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiAIEngine : AIEngine {
    
    private suspend fun askGeminiJson(prompt: String, systemPrompt: String? = null, jsonSchemaType: Class<*>): Any? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Missing Gemini API Key. Please configure in .env")
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            ),
            systemInstruction = systemPrompt?.let { Content(parts = listOf(Part(text = it))) }
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw IllegalStateException("Empty response from AI")
        
        // Use Moshi to parse the JSON string response
        val adapter = RetrofitClient.moshi.adapter(jsonSchemaType)
        adapter.fromJson(textResponse)
    }

    override suspend fun analyzeLearningMaterial(input: String): LearningAnalysis {
        val sysPrompt = "You are an expert tutor. Analyze the following educational material. Respond in JSON strictly matching this schema: { \"subject\": \"...\", \"topic\": \"...\", \"keyConcepts\": [\"...\"], \"difficulty\": \"...\", \"shortSummary\": \"...\", \"studentGoal\": \"...\" }"
        val result = askGeminiJson(input, sysPrompt, LearningAnalysis::class.java) as LearningAnalysis
        return result
    }

    override suspend fun generateQuiz(topic: String, material: String): List<QuizQuestion> {
        val prompt = "Generate 3 multiple-choice questions for the topic: \$topic based on the material: \$material. Question 1 basic, Question 2 application, Question 3 slightly harder reasoning."
        val sysPrompt = "Respond in JSON strictly matching this schema: { \"questions\": [ { \"id\": \"...\", \"question\": \"...\", \"options\": [\"...\", \"...\"], \"correctOptionIndex\": 0, \"explanation\": \"...\" } ] }"
        
        // Define a wrapper for the list
        val result = askGeminiJson(prompt, sysPrompt, QuizListWrapper::class.java) as QuizListWrapper
        return result.questions
    }

    override suspend fun analyzeQuizResults(topic: String, score: Int, total: Int, material: String, userAnswers: List<String>): LearningGap {
        val prompt = "Student scored \$score/\$total on topic \$topic. Their answers were: \${userAnswers.joinToString()}. Analyze their learning gap."
        val sysPrompt = "Respond in JSON strictly matching this schema: { \"score\": 0, \"total\": 3, \"conceptsUnderstood\": [\"...\"], \"conceptsToImprove\": [\"...\"], \"weakestConcept\": \"...\", \"gapMessage\": \"...\" }"
        val result = askGeminiJson(prompt, sysPrompt, LearningGap::class.java) as LearningGap
        return result
    }

    override suspend fun generateFollowUpQuestion(weakConcept: String): QuizQuestion {
        val prompt = "Generate 1 targeted follow-up question specifically about the weak concept: \$weakConcept."
        val sysPrompt = "Respond in JSON strictly matching this schema: { \"id\": \"...\", \"question\": \"...\", \"options\": [\"...\", \"...\"], \"correctOptionIndex\": 0, \"explanation\": \"...\" }"
        val result = askGeminiJson(prompt, sysPrompt, QuizQuestion::class.java) as QuizQuestion
        return result
    }
}

@JsonClass(generateAdapter = true)
data class QuizListWrapper(
    val questions: List<QuizQuestion>
)
