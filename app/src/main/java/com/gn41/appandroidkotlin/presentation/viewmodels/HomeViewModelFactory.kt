package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.ReservationsRepository
import com.gn41.appandroidkotlin.data.repositories.RidesRepository

class HomeViewModelFactory(
    private val ridesRepository: RidesRepository,
    private val sessionManager: SessionManager,
    private val reservationsRepository: ReservationsRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(ridesRepository, sessionManager, reservationsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
