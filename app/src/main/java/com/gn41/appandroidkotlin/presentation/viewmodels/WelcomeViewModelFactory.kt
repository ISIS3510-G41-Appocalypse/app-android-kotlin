package com.gn41.appandroidkotlin.presentation.viewmodels


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.AuthRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository

class WelcomeViewModelFactory(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WelcomeViewModel::class.java)) {
            val networkHelper = NetworkHelper(context)
            return WelcomeViewModel(
                authRepository = authRepository,
                sessionManager = sessionManager,
                tripRepository = tripRepository,
                networkHelper = networkHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}