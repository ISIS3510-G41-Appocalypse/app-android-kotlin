package com.gn41.appandroidkotlin.presentation.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.ui.theme.AppAndroidKotlinTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gn41.appandroidkotlin.data.repositories.AuthRepositoryImpl
import com.gn41.appandroidkotlin.data.services.AuthService
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppAndroidKotlinTheme {
                val authService = AuthService()
                val authRepository = AuthRepositoryImpl(authService)
                val factory = WelcomeViewModelFactory(authRepository)
                val welcomeViewModel: WelcomeViewModel = viewModel(factory = factory)
                WelcomeScreen(welcomeViewModel)

            }
        }
    }
}


















