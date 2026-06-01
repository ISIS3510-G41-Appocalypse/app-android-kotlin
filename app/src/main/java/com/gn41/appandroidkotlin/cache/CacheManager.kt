package com.gn41.appandroidkotlin.cache

import android.util.ArrayMap
import android.util.Log
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto

object CacheManager {
    private val cacheFormState : ArrayMap<String,String> = ArrayMap()

    private val ridesRiderPayments : MutableList<RidePaymentDto> = mutableListOf()

    private val ridesDriverPayments : MutableList<RidePaymentDto> = mutableListOf()

    private val paymentsRider : MutableMap<Int, List<PaymentDto>> = mutableMapOf()

    private val paymentsDriver : MutableMap<Int, List<PaymentDto>> = mutableMapOf()

    private var montoRider : Int = 0

    private var montoDriver : Int = 0


    fun putFormState(key: String, value: String) {
        cacheFormState[key] = value
    }

    fun getFormState(key: String): String? {
        return cacheFormState[key]
    }

    fun containsKeyFormState(key: String): Boolean {
        return cacheFormState.containsKey(key)
    }

    fun clearFormState() {
        cacheFormState.clear()
    }

    fun getForm(): ArrayMap<String,String>? {
        return cacheFormState
    }

    fun setForm(form: ArrayMap<String,String>) {
        cacheFormState.clear()
        cacheFormState.putAll(form)
    }

    fun getRidesRiderPayments(): MutableList<RidePaymentDto> {
        return ridesRiderPayments
    }

    fun getRidesDriverPayments(): MutableList<RidePaymentDto> {
        return ridesDriverPayments
    }

    fun getPaymentsRider(): MutableMap<Int, List<PaymentDto>> {
        return paymentsRider
    }

    fun getPaymentsDriver(): MutableMap<Int, List<PaymentDto>> {
        return paymentsDriver
    }

    fun setRidesRiderPayments(rides: MutableList<RidePaymentDto>) {
        ridesRiderPayments.clear()
        ridesRiderPayments.addAll(rides)
    }

    fun setRidesDriverPayments(rides: MutableList<RidePaymentDto>) {
        ridesDriverPayments.clear()
        ridesDriverPayments.addAll(rides)
        Log.d("Cache", "ridesDriverPayments size: $ridesDriverPayments")
    }

    fun setPaymentsRider(payments: MutableMap<Int, List<PaymentDto>>) {
        paymentsRider.clear()
        paymentsRider.putAll(payments)
    }

    fun setPaymentsDriver(payments: MutableMap<Int, List<PaymentDto>>) {
        paymentsDriver.clear()
        paymentsDriver.putAll(payments)
    }

    fun getMontoDriver(): Int {
        return montoDriver
    }

    fun getMontoRider(): Int {
        return montoRider
    }

    fun setMontoDriver(monto: Int) {
        montoDriver = monto
    }

    fun setMontoRider(monto: Int) {
        montoRider = monto
    }

}