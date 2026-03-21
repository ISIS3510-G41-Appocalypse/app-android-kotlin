package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.AuthRepository
import com.gn41.appandroidkotlin.data.repositories.TripRepository

class WelcomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WelcomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WelcomeViewModel(authRepository, sessionManager, tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}