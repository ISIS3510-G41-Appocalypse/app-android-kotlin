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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppAndroidKotlinTheme {
                val welcomeViewModel: WelcomeViewModel = viewModel()
                WelcomeScreen(
                    showLoginCard = welcomeViewModel.showLoginCard,
                    onLoginClick = { welcomeViewModel.onLoginClicked();println("Presionaste Login")},
                    onRegisterClick = { welcomeViewModel.onRegisterClicked();println("Presionaste Register")} )

            }
        }
    }
}


















