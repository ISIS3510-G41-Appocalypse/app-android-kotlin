package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.core.connectivity.NetworkHelper
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.data.services.payments.PaymentsService
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager

class PaymentsRepository (private val paymentsService: PaymentsService,
    private val networkHelper: NetworkHelper,
    private val localStorageManager: LocalStorageManager
) {
    suspend fun getRides(selectedRole: String) : List<RidePaymentDto> {
        return paymentsService.getRides(selectedRole)
    }

    suspend fun getPayments(selectedRole: String, rideId: Int) : List<PaymentDto> {
        return paymentsService.getPayments(selectedRole, rideId)
    }

    suspend fun pay(id:Int, selectedMethod: String) {
        paymentsService.pay(id,selectedMethod)
    }

    suspend fun rechazarPago(id:Int) {
        paymentsService.rechazarPago(id)
    }

    suspend fun confirmarPago(id:Int) {
        paymentsService.confirmarPago(id)
    }

    fun availableConnection(): Boolean {
        return networkHelper.isInternetAvailable()
    }

    suspend fun saveCache() {
        localStorageManager.savePaymentsState()
    }

    suspend fun readLocalStorage() {
        localStorageManager.readPaymentsState()
    }

    suspend fun clearLocalStorage() {
        localStorageManager.clearPaymentsState()
    }

    suspend fun create(rideId:Int) {
        paymentsService.create(rideId)
    }

    suspend fun getMonto( payments: MutableMap<Int, List<PaymentDto>>): Int {
        return paymentsService.getMonto( payments)
    }
}