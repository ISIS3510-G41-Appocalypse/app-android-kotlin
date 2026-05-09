package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.LocationRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager

class TripViewModelFactory(
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager,
    private val locationRepository: LocationRepository,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(
                tripRepository,
                sessionManager,
                locationRepository,
                networkHelper,
                localStorageManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}