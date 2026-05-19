package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager

class SettingsViewModelFactory(
    private val sessionManager: SessionManager,
    private val localStorageManager: LocalStorageManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(sessionManager, localStorageManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
