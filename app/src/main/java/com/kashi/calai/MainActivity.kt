package com.kashi.calai

import CalorieOverviewScreen
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kashi.calai.ui.screens.ResultScreen
import com.kashi.calai.ui.theme.CalAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalAITheme {
                var permissionGranted by remember { mutableStateOf(false) }

                // Camera permission launcher
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    permissionGranted = isGranted
                }

                // Request permission on launch
                LaunchedEffect(Unit) {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }

                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (permissionGranted) {
                        NavHost(navController = navController, startDestination = "overview") {
                            composable("overview") { CalorieOverviewScreen(navController) }
                            composable("camera") { CameraScreen(navController) }
                            composable(
                                "resultscreen/{imageUri}",
                                arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val uri = backStackEntry.arguments?.getString("imageUri") ?: ""
                                ResultScreen(navController, uri)
                            }
                        }
                    } else {
                        PermissionDeniedScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen() {
    androidx.compose.material3.Text("Camera permission denied. Please restart and allow access.")
}
