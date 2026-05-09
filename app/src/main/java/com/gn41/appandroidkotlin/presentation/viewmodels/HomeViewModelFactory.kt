package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepository
import com.gn41.appandroidkotlin.data.repositories.RidesRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager

class HomeViewModelFactory(
    private val ridesRepository: RidesRepository,
    private val sessionManager: SessionManager,
    private val reservationsRepository: ReservationsRepository? = null,
    private val tripRepository: TripRepository? = null,
    private val zoneRepository: ZoneRepository,
    private val vehicleRepository: VehicleRepository,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                ridesRepository,
                sessionManager,
                reservationsRepository,
                tripRepository,
                zoneRepository,
                vehicleRepository,
                networkHelper,
                localStorageManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
