package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.RatingRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager

class RatingViewModelFactory(
    private val tripRepository: TripRepository,
    private val ratingRepository: RatingRepository,
    private val sessionManager: SessionManager,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RatingViewModel(
                tripRepository = tripRepository,
                ratingRepository = ratingRepository,
                sessionManager = sessionManager,
                networkHelper = networkHelper,
                localStorageManager = localStorageManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

