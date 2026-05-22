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
import com.gn41.appandroidkotlin.presentation.viewmodels.RegisterViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.RegisterViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.SettingsViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.SettingsViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.presentation.views.CreateRideScreen
import com.gn41.appandroidkotlin.presentation.views.HomeScreen
import com.gn41.appandroidkotlin.presentation.views.PaymentsScreen
import com.gn41.appandroidkotlin.presentation.views.RateUserScreen
import com.gn41.appandroidkotlin.presentation.views.RegisterScreen
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
    registerViewModelFactory: RegisterViewModelFactory,
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

            WelcomeScreen(viewModel = welcomeViewModel, onRegisterClick = { navController.navigate("register") })
        }

        composable("register") {
            val registerViewModel: RegisterViewModel = viewModel(factory = registerViewModelFactory)
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegistrationSuccess = { email, password ->

                    welcomeViewModel.onEmailInput(email)
                    welcomeViewModel.onPasswordInput(password)

                    welcomeViewModel.onLoginSubmit {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                viewModel = registerViewModel
            )
        }

        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)
            HomeScreen(
                viewModel = homeViewModel,
                onTripsClick = {
                    navController.navigate("trips")
                },
                onPagosClick = {
                    navController.navigate("payments")
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
            val riderRatingFinished = backStackEntry.savedStateHandle.get<Boolean>("rider_rating_finished") ?: false
            val driverRatingFinished = backStackEntry.savedStateHandle.get<Boolean>("driver_rating_finished") ?: false

            LaunchedEffect(riderRatingFinished, driverRatingFinished) {
                if (riderRatingFinished) {
                    // Rating completed: clear without marking as skipped
                    tripViewModel.completeFinishedRideForRating()
                    backStackEntry.savedStateHandle["rider_rating_finished"] = false
                }
                if (driverRatingFinished) {
                    // Rating completed: clear without marking as skipped
                    tripViewModel.completeFinishedRiderRideForRating()
                    backStackEntry.savedStateHandle["driver_rating_finished"] = false
                }
            }

            TripScreen(
                viewModel = tripViewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                onPagosClick = {
                    navController.navigate("payments")
                },
                onRateRidersClick = { rideId ->
                    navController.navigate("rate_user/$rideId/rider")
                },
                onRateDriverClick = { rideId ->
                    navController.navigate("rate_user/$rideId/driver")
                }
            )
        }

        composable("payments") {
            PaymentsScreen(
                onHomeClick = {
                    navController.navigate("home")
                },
                onTripsClick = {
                    navController.navigate("trips")
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
                        ?.set(if (ratingType == "driver") "driver_rating_finished" else "rider_rating_finished", true)
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