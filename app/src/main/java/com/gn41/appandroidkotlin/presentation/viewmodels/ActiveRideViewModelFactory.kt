package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.RideRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository

class ActiveRideViewModelFactory(
    private val rideRepository: RideRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveRideViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActiveRideViewModel(rideRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}