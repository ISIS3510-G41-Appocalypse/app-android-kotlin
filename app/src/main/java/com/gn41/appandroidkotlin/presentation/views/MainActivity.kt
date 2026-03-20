package com.gn41.appandroidkotlin.presentation.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.AuthRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.RideRepository
import com.gn41.appandroidkotlin.data.repositories.RideRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.RidesRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepositoryImpl
import com.gn41.appandroidkotlin.data.services.auth.AuthService
import com.gn41.appandroidkotlin.data.services.rides.RideService
import com.gn41.appandroidkotlin.data.services.rides.RidesService
import com.gn41.appandroidkotlin.data.services.userId.UserIdService
import com.gn41.appandroidkotlin.data.services.vehicles.VehicleService
import com.gn41.appandroidkotlin.data.services.zones.ZoneService
import com.gn41.appandroidkotlin.presentation.navigation.AppNavigation
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModelFactory
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
                val homeFactory = HomeViewModelFactory(ridesRepository, sessionManager)

                val userIdService = UserIdService(sessionManager)
                val rideService = RideService(sessionManager, userIdService)
                val vehicleService = VehicleService(sessionManager, userIdService)
                val zoneService = ZoneService(sessionManager)
                val rideRepository = RideRepositoryImpl(rideService)
                val vehicleRepository = VehicleRepositoryImpl(vehicleService)
                val zoneRepository = ZoneRepositoryImpl(zoneService)
                val createRideViewModelFactory = CreateRideViewModelFactory(rideRepository,vehicleRepository,zoneRepository)
                val createRideViewModel: CreateRideViewModel = viewModel(factory = createRideViewModelFactory)

                val navController = rememberNavController()

                AppNavigation(
                    navController = navController,
                    welcomeViewModel = welcomeViewModel,
                    homeViewModelFactory = homeFactory,
                    createRideViewModel = createRideViewModel
                )
            }
        }
    }
}