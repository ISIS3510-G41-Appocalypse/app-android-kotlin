package com.gn41.appandroidkotlin.presentation.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.AuthRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.RideRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.RidesRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.TripRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.VehicleRepositoryImpl
import com.gn41.appandroidkotlin.data.repositories.ZoneRepositoryImpl
import com.gn41.appandroidkotlin.data.services.auth.AuthService
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsService
import com.gn41.appandroidkotlin.data.services.rides.RideService
import com.gn41.appandroidkotlin.data.services.rides.RidesService
import com.gn41.appandroidkotlin.data.services.trips.TripService
import com.gn41.appandroidkotlin.data.services.userId.UserIdService
import com.gn41.appandroidkotlin.data.services.vehicles.VehicleService
import com.gn41.appandroidkotlin.data.services.zones.ZoneService
import com.gn41.appandroidkotlin.presentation.navigation.AppNavigation
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModelFactory
import com.gn41.appandroidkotlin.ui.theme.AppAndroidKotlinTheme
import com.gn41.appandroidkotlin.data.services.location.LocationService
import com.gn41.appandroidkotlin.data.repositories.LocationRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppAndroidKotlinTheme {
                val sessionManager = SessionManager(this)

                val authService = AuthService()
                val authRepository = AuthRepositoryImpl(authService)

                val tripService = TripService()
                val tripRepository = TripRepositoryImpl(tripService)

                val welcomeFactory = WelcomeViewModelFactory(
                    context = this,
                    authRepository = authRepository,
                    sessionManager = sessionManager,
                    tripRepository = tripRepository
                )
                val welcomeViewModel: WelcomeViewModel = viewModel(factory = welcomeFactory)

                val reservationsService = ReservationsService()
                val reservationsRepository = ReservationsRepositoryImpl(reservationsService)

                val ridesService = RidesService()
                val ridesRepository = RidesRepositoryImpl(ridesService)
                val locationService = LocationService()
                val locationRepository = LocationRepositoryImpl(locationService)

                val tripViewModelFactory = TripViewModelFactory(
                    tripRepository = tripRepository,
                    sessionManager = sessionManager,
                    locationRepository = locationRepository
                )

                val userIdService = UserIdService(sessionManager)
                val rideService = RideService(sessionManager, userIdService)
                val vehicleService = VehicleService(sessionManager, userIdService)
                val zoneService = ZoneService(sessionManager)

                val rideRepository = RideRepositoryImpl(rideService)
                val vehicleRepository = VehicleRepositoryImpl(vehicleService)
                val zoneRepository = ZoneRepositoryImpl(zoneService)

                val homeFactory = HomeViewModelFactory(
                    ridesRepository = ridesRepository,
                    reservationsRepository = reservationsRepository,
                    sessionManager = sessionManager,
                    tripRepository = tripRepository,
                    vehicleRepository = vehicleRepository
                )


                val createRideViewModelFactory = CreateRideViewModelFactory(
                    rideRepository = rideRepository,
                    vehicleRepository = vehicleRepository,
                    zoneRepository = zoneRepository,
                    sessionManager = sessionManager
                )

                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    com.gn41.appandroidkotlin.data.local.SessionEvents.onSessionExpired.collect {
                        sessionManager.clearToken()
                        sessionManager.clearUserId()

                        welcomeViewModel.resetLoginState()

                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                AppNavigation(
                    navController = navController,
                    welcomeViewModel = welcomeViewModel,
                    homeViewModelFactory = homeFactory,
                    createRideViewModelFactory = createRideViewModelFactory,
                    tripViewModelFactory = tripViewModelFactory
                )
            }
        }
    }
}