package com.gn41.appandroidkotlin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRideViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRideViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.presentation.views.ActiveRideScreen
import com.gn41.appandroidkotlin.presentation.views.CreateRideScreen
import com.gn41.appandroidkotlin.presentation.views.HomeScreen
import com.gn41.appandroidkotlin.presentation.views.TripScreen
import com.gn41.appandroidkotlin.presentation.views.WelcomeScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    welcomeViewModel: WelcomeViewModel,
    homeViewModelFactory: HomeViewModelFactory,
    createRideViewModelFactory: CreateRideViewModelFactory,
    activeRideViewModelFactory: ActiveRideViewModelFactory
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
            val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)
            HomeScreen(
                viewModel = homeViewModel,
                onTripsClick = {
                    navController.navigate("trips")
                },
                onCreateRideClick = {
                    // FASE 4: la validación de internet vive en el ViewModel
                    homeViewModel.onCreateRideRequested {
                        navController.navigate("create_ride")
                    }
                },
                onLogoutClick = {
                    welcomeViewModel.resetLoginState()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("trips") {
            val activeRideViewModel: ActiveRideViewModel = viewModel(factory = activeRideViewModelFactory)
            ActiveRideScreen(
                viewModel = activeRideViewModel,
                onBackClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                }
            )
        }
/*
        composable("trips") {
            val tripViewModel: TripViewModel = viewModel(factory = tripViewModelFactory)
            TripScreen(
                viewModel = tripViewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                }
            )
        }
        */

        composable("create_ride") {
            val createRideViewModel: CreateRideViewModel = viewModel(factory = createRideViewModelFactory)
            CreateRideScreen(
                viewModel = createRideViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
