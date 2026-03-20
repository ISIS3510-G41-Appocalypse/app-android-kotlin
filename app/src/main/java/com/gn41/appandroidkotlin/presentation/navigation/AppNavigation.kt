package com.gn41.appandroidkotlin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.presentation.views.HomeScreen
import com.gn41.appandroidkotlin.presentation.views.WelcomeScreen
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AppNavigation(
    navController: NavHostController,
    welcomeViewModel: WelcomeViewModel,
    homeViewModel: HomeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {

        composable("welcome") {
            LaunchedEffect(welcomeViewModel.isLoggedIn) {
                if (welcomeViewModel.isLoggedIn) {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            }
            WelcomeScreen(viewModel = welcomeViewModel)
        }

        composable("home") {
            HomeScreen(viewModel = homeViewModel)
        }
    }
}