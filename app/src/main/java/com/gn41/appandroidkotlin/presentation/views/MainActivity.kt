package com.gn41.appandroidkotlin.presentation.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.AuthRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.RidesRepositoryImpl
import com.gn41.appandroidkotlin.data.services.auth.AuthService
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsService
import com.gn41.appandroidkotlin.data.services.rides.RidesService
import com.gn41.appandroidkotlin.presentation.navigation.AppNavigation
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModelFactory
import com.gn41.appandroidkotlin.ui.theme.AppAndroidKotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppAndroidKotlinTheme {
                val sessionManager = SessionManager(this)

                val authService = AuthService()
                val authRepository = AuthRepositoryImpl(authService)
                val welcomeFactory = WelcomeViewModelFactory(authRepository, sessionManager)
                val welcomeViewModel: WelcomeViewModel = viewModel(factory = welcomeFactory)

                val ridesService = RidesService()
                val ridesRepository = RidesRepositoryImpl(ridesService)

                val reservationsService = ReservationsService()
                val reservationsRepository = ReservationsRepositoryImpl(reservationsService)

                val homeFactory = HomeViewModelFactory(ridesRepository, sessionManager, reservationsRepository)

                val navController = rememberNavController()

                AppNavigation(
                    navController = navController,
                    welcomeViewModel = welcomeViewModel,
                    homeViewModelFactory = homeFactory
                )
            }
        }
    }
}