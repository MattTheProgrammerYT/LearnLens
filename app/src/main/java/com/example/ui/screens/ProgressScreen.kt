package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.LearningSession
import com.example.model.ProgressProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(onBack: () -> Unit) {
    val mockProfile = ProgressProfile(
        topicsStudied = 12,
        averageScorePercent = 85,
        strongConcepts = listOf("Cellular Respiration", "Newton's Laws", "Basic Algebra"),
        weakConcepts = listOf("Photosynthesis - Calvin Cycle", "Calculus Limits"),
        recentSessions = listOf(
            LearningSession("Cellular Respiration", 90, "Yesterday"),
            LearningSession("Newton's Laws", 80, "2 days ago")
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.SemiBold) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(title = "Topics", value = mockProfile.topicsStudied.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                StatCard(title = "Avg Score", value = "${mockProfile.averageScorePercent}%", modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Strong Concepts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            mockProfile.strongConcepts.forEach { 
                Text("- $it", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Needs Improvement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
            mockProfile.weakConcepts.forEach { 
                Text("- $it", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
