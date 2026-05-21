package com.gn41.appandroidkotlin.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.RatingViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.RatingViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.SettingsViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.SettingsViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.presentation.views.CreateRideScreen
import com.gn41.appandroidkotlin.presentation.views.HomeScreen
import com.gn41.appandroidkotlin.presentation.views.RateUserScreen
import com.gn41.appandroidkotlin.presentation.views.SettingsScreen
import com.gn41.appandroidkotlin.presentation.views.TripScreen
import com.gn41.appandroidkotlin.presentation.views.WelcomeScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    navController: NavHostController,
    welcomeViewModel: WelcomeViewModel,
    homeViewModelFactory: HomeViewModelFactory,
    createRideViewModelFactory: CreateRideViewModelFactory,
    tripViewModelFactory: TripViewModelFactory,
    ratingViewModelFactory: RatingViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory,
    onDarkModeChanged: (Boolean) -> Unit
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
                    homeViewModel.onCreateRideRequested {
                        navController.navigate("create_ride")
                    }
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    welcomeViewModel.resetLoginState()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDarkModeChanged = onDarkModeChanged
            )
        }

        composable("trips") { backStackEntry ->
            val tripViewModel: TripViewModel = viewModel(factory = tripViewModelFactory)
            val ratingFinished = backStackEntry.savedStateHandle.get<Boolean>("rating_finished") ?: false

            LaunchedEffect(ratingFinished) {
                if (ratingFinished) {
                    tripViewModel.clearFinishedRideForRating()
                    backStackEntry.savedStateHandle["rating_finished"] = false
                }
            }

            TripScreen(
                viewModel = tripViewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                onRateRidersClick = { rideId ->
                    navController.navigate("rate_user/$rideId/rider")
                }
            )
        }


        composable("rate_user/{rideId}/{ratingType}") { backStackEntry ->
            val ratingViewModel: RatingViewModel = viewModel(factory = ratingViewModelFactory)
            val rideId = backStackEntry.arguments?.getString("rideId")?.toIntOrNull() ?: 0
            val ratingType = backStackEntry.arguments?.getString("ratingType") ?: "driver"

            RateUserScreen(
                viewModel = ratingViewModel,
                rideId = rideId,
                ratingType = ratingType,
                onBack = { navController.popBackStack() },
                onRatingFinished = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("rating_finished", true)
                }
            )
        }


        composable("create_ride") {
            val createRideViewModel: CreateRideViewModel = viewModel(factory = createRideViewModelFactory)
            CreateRideScreen(
                viewModel = createRideViewModel,
                onBackClick = { navController.popBackStack() },
                onRideCreated = {
                    navController.navigate("trips") {
                        popUpTo("create_ride") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
