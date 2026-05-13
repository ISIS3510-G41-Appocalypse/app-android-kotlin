package com.gn41.appandroidkotlin.presentation.views

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepository
import com.gn41.appandroidkotlin.data.repositories.RideRepository
import com.gn41.appandroidkotlin.data.repositories.RidesRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository
import com.gn41.appandroidkotlin.data.services.auth.AuthService
import com.gn41.appandroidkotlin.data.services.reservations.ReservationsService
import com.gn41.appandroidkotlin.data.services.rides.RideService
import com.gn41.appandroidkotlin.data.services.rides.RidesService
import com.gn41.appandroidkotlin.data.services.trips.TripService
import com.gn41.appandroidkotlin.data.services.userId.UserIdService
import com.gn41.appandroidkotlin.data.services.vehicles.VehicleService
import com.gn41.appandroidkotlin.data.services.zones.ZoneService
import com.gn41.appandroidkotlin.presentation.navigation.AppNavigation
import com.gn41.appandroidkotlin.presentation.cache.TripMemoryCache
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModelFactory
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModelFactory
import com.gn41.appandroidkotlin.ui.theme.AppAndroidKotlinTheme
import com.gn41.appandroidkotlin.data.services.location.LocationService
import com.mapbox.common.MapboxOptions
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.repositories.AuthRepository
import com.gn41.appandroidkotlin.data.repositories.LocationRepository
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        setContent {
            AppAndroidKotlinTheme {
                val sessionManager = SessionManager(this)
                val networkHelper = NetworkHelper(this)
                val localStorageManager = LocalStorageManager(this)

                val authService = AuthService()
                val authRepository = AuthRepository(authService)

                val tripService = TripService()
                val tripRepository = TripRepository(tripService, networkHelper)

                val welcomeFactory = WelcomeViewModelFactory(
                    context = this,
                    authRepository = authRepository,
                    sessionManager = sessionManager,
                    tripRepository = tripRepository
                )
                val welcomeViewModel: WelcomeViewModel = viewModel(factory = welcomeFactory)

                val reservationsService = ReservationsService()
                val reservationsRepository = ReservationsRepository(reservationsService)

                val ridesService = RidesService()
                val ridesRepository = RidesRepository(ridesService)
                val locationService = LocationService()
                val locationRepository = LocationRepository(locationService, sessionManager)

                val tripViewModelFactory = TripViewModelFactory(
                    tripRepository = tripRepository,
                    sessionManager = sessionManager,
                    locationRepository = locationRepository,
                    networkHelper = networkHelper,
                    localStorageManager = localStorageManager
                )

                val userIdService = UserIdService(sessionManager)
                val rideService = RideService(sessionManager, userIdService)
                val vehicleService = VehicleService(sessionManager, userIdService)
                val zoneService = ZoneService(sessionManager)

                val rideRepository = RideRepository(rideService, networkHelper, localStorageManager)
                val vehicleRepository = VehicleRepository(vehicleService)
                val zoneRepository = ZoneRepository(zoneService)

                val homeFactory = HomeViewModelFactory(
                    ridesRepository = ridesRepository,
                    reservationsRepository = reservationsRepository,
                    sessionManager = sessionManager,
                    tripRepository = tripRepository,
                    zoneRepository = zoneRepository,
                    vehicleRepository = vehicleRepository,
                    networkHelper = networkHelper,
                    localStorageManager = localStorageManager
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
                        TripMemoryCache.clear()
                        localStorageManager.clearTripState()

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
                    /*activeRideViewModelFactory = activeRideViewModelFactory*/
                    tripViewModelFactory = tripViewModelFactory
                )
            }
        }
    }
}

/*2.0.0*/