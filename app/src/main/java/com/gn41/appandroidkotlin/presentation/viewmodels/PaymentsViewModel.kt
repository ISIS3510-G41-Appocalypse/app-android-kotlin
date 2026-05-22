package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.ArrayMap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.data.repositories.PaymentsRepository
import kotlinx.coroutines.launch

class PaymentsViewModel ( private val paymentsRepository: PaymentsRepository) : ViewModel() {
    var connectivity by mutableStateOf<Boolean>(false)
        private set
    var selectedRole by mutableStateOf<String>("Conductor")
        private set

    var rides by mutableStateOf<List<RidePaymentDto>>(emptyList())
        private set

    var payments by mutableStateOf<ArrayMap<Int, List<PaymentDto>>>(ArrayMap())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            rides = paymentsRepository.getRides(selectedRole)
            if (rides.isNotEmpty()) {
                rides.forEach { ride ->
                    payments[ride.id] = paymentsRepository.getPayments(selectedRole, ride.id)
                }
            }
        }
        Log.d("PaymentsViewModel", "Rides: $rides")
        Log.d("PaymentsViewModel", "Payments: $payments")
    }

    fun onRoleChange(value: String) {
        selectedRole = value
        loadData()
    }

    fun onPayClicked( ) {}
}