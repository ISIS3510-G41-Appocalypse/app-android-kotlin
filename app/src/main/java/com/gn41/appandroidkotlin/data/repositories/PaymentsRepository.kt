package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.data.services.payments.PaymentsService

class PaymentsRepository (private val paymentsService: PaymentsService) {
    suspend fun getRides(selectedRole: String) : List<RidePaymentDto> {
        return paymentsService.getRides(selectedRole)
    }

    suspend fun getPayments(selectedRole: String, rideId: Int) : List<PaymentDto> {
        return paymentsService.getPayments(selectedRole, rideId)
    }
}