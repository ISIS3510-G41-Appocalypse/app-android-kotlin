package com.gn41.appandroidkotlin.presentation.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.ui.theme.AppAndroidKotlinTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.AuthRepositoryImpl
import com.gn41.appandroidkotlin.data.services.auth.AuthService
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModelFactory
import androidx.navigation.compose.rememberNavController
import com.gn41.appandroidkotlin.presentation.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppAndroidKotlinTheme {
                val authService = AuthService()
                val authRepository = AuthRepositoryImpl(authService)
                val sessionManager = SessionManager(this)
                val factory = WelcomeViewModelFactory(authRepository,sessionManager)
                val welcomeViewModel: WelcomeViewModel = viewModel(factory = factory)

                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    welcomeViewModel = welcomeViewModel
                )

            }
        }
    }
}


















