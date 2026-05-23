package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gn41.appandroidkotlin.data.repositories.PaymentsRepository

class PaymentsViewModelFactory (
    private val paymentsRepository: PaymentsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentsViewModel(paymentsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}