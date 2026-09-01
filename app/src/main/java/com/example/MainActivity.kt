package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LearnLensApp()
            }
        }
    }
}

@Composable
fun LearnLensApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onScanClicked = {
                    viewModel.resetState()
                    navController.navigate("scan")
                },
                onProgressClicked = { navController.navigate("progress") }
            )
        }
        composable("scan") {
            ScanScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAnalysisReady = { navController.navigate("learn") {
                    popUpTo("home")
                } }
            )
        }
        composable("learn") {
            LearnScreen(
                viewModel = viewModel,
                onBack = { navController.navigate("home") { popUpTo(0) } },
                onQuizRequested = { navController.navigate("quiz") }
            )
        }
        composable("quiz") {
            val analysis = (viewModel.uiState.value as? com.example.ui.UiState.QuizReady)
            QuizScreen(
                viewModel = viewModel,
                topic = "Photosynthesis", // In a real app this would be state driven
                onBack = { navController.popBackStack() },
                onQuizFinished = { navController.navigate("gap") {
                    popUpTo("learn")
                } }
            )
        }
        composable("gap") {
            GapAnalysisScreen(
                viewModel = viewModel,
                onBackToHome = { navController.navigate("home") { popUpTo(0) } },
                onFollowUpReady = { navController.navigate("quiz") } // Loop back to quiz for follow up
            )
        }
        composable("progress") {
            ProgressScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
