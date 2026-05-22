package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.ArrayMap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.data.repositories.PaymentsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PaymentsViewModel ( private val paymentsRepository: PaymentsRepository) : ViewModel() {
    var connectivity by mutableStateOf<Boolean>(false)
        private set
    var selectedRole by mutableStateOf<String>("Conductor")
        private set

    var rides by mutableStateOf<List<RidePaymentDto>>(emptyList())
        private set

    val payments = mutableStateMapOf<Int, List<PaymentDto>>()

    var isLoadingData by mutableStateOf(false)
        private set

    init {
        loadData()
    }

    private fun loadData() {
        rides = emptyList()
        payments.clear()
        viewModelScope.launch {
            isLoadingData = true
            rides = paymentsRepository.getRides(selectedRole)
            if (rides.isNotEmpty()) {
                rides.forEach { ride ->
                    payments[ride.id] = async{ paymentsRepository.getPayments(selectedRole, ride.id) }.await()
                }
            }
            Log.d("PaymentsViewModel", "Rides: $rides")
            Log.d("PaymentsViewModel", "Payments: $payments")
            isLoadingData = false
        }
    }

    fun onRoleChange(value: String) {
        selectedRole = value
        loadData()
    }

    fun onPayClicked( id:Int, selectedMethod: String) {
        viewModelScope.launch {
            paymentsRepository.pay(id,selectedMethod)
        }
    }

    fun onRechazarPago(id:Int) {
        viewModelScope.launch {
            paymentsRepository.rechazarPago(id)
        }
    }

    fun onConfirmarPago(id:Int) {
        viewModelScope.launch {
            paymentsRepository.confirmarPago(id)
        }
    }
}