package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.local.RegisterDraftManager

class RegisterViewModelFactory(
    private val registerDraftManager: RegisterDraftManager,
    private val networkHelper: NetworkHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(
                registerDraftManager = registerDraftManager,
                networkHelper = networkHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}