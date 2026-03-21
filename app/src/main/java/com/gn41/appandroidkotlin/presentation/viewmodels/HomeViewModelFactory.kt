package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepository
import com.gn41.appandroidkotlin.data.repositories.RidesRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository

class HomeViewModelFactory(
    private val ridesRepository: RidesRepository,
    private val sessionManager: SessionManager,
    private val reservationsRepository: ReservationsRepository? = null,
    private val tripRepository: TripRepository? = null,
    private val vehicleRepository: VehicleRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(ridesRepository, sessionManager, reservationsRepository, tripRepository, vehicleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
