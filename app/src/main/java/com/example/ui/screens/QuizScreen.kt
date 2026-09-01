package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.UiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: MainViewModel,
    topic: String,
    onBack: () -> Unit,
    onQuizFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Generating adaptive quiz...")
            }
        }
        return
    }

    val questions = when (uiState) {
        is UiState.QuizReady -> (uiState as UiState.QuizReady).questions
        is UiState.FollowUpQuestionReady -> listOf((uiState as UiState.FollowUpQuestionReady).question)
        else -> return
    }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentQuestionIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz: $topic", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Progress
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1) / questions.size.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Question ${currentQuestionIndex + 1} of ${questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            currentQuestion.options.forEachIndexed { index, option ->
                val isSelected = selectedOptionIndex == index
                val isCorrectOption = index == currentQuestion.correctOptionIndex
                val showAsCorrect = showExplanation && isCorrectOption
                val showAsWrong = showExplanation && isSelected && !isCorrectOption

                val bgColor = when {
                    showAsCorrect -> Color(0xFFE8F5E9)
                    showAsWrong -> Color(0xFFFFEBEE)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
                
                val borderColor = when {
                    showAsCorrect -> Color(0xFF4CAF50)
                    showAsWrong -> Color(0xFFF44336)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable(enabled = !showExplanation) {
                            selectedOptionIndex = index
                        }
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (showExplanation) {
                            if (showAsCorrect) {
                                Icon(Icons.Default.CheckCircle, tint = Color(0xFF4CAF50), contentDescription = null)
                            } else if (showAsWrong) {
                                Icon(Icons.Default.Error, tint = Color(0xFFF44336), contentDescription = null)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            
            if (showExplanation) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Explanation", 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            currentQuestion.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = {
                    if (!showExplanation) {
                        val isCorrect = selectedOptionIndex == currentQuestion.correctOptionIndex
                        viewModel.recordAnswer(currentQuestion.options[selectedOptionIndex ?: 0], isCorrect)
                        showExplanation = true
                    } else {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedOptionIndex = null
                            showExplanation = false
                        } else {
                            viewModel.finishQuiz(topic, questions.size)
                            onQuizFinished()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedOptionIndex != null
            ) {
                Text(
                    text = if (!showExplanation) "Check Answer" else if (currentQuestionIndex < questions.size - 1) "Next Question" else "See Results",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
