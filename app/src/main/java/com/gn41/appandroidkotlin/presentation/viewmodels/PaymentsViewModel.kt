package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.ArrayMap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.cache.CacheManager
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

    val rides : MutableList<RidePaymentDto> = mutableStateListOf()

    val payments : MutableMap<Int, List<PaymentDto>> = mutableStateMapOf()

    val ridesOtro : MutableList<RidePaymentDto> = mutableStateListOf()

    val paymentsOtro : MutableMap<Int, List<PaymentDto>> = mutableStateMapOf()

    var isLoadingData by mutableStateOf(false)
        private set

    var monto by mutableIntStateOf(-1)
        private set

    var montoOtro by mutableIntStateOf(-1)
        private set

    init {
        loadData()
    }

    private fun loadData() {
        rides.clear()
        payments.clear()
        ridesOtro.clear()
        paymentsOtro.clear()
        connectivity = paymentsRepository.availableConnection()
        if (connectivity) {
            viewModelScope.launch {
                isLoadingData = true
                rides.addAll(paymentsRepository.getRides(selectedRole))
                if (rides.isNotEmpty()) {
                    rides.forEach { ride ->
                        payments[ride.id] =
                            async {
                                paymentsRepository.getPayments(
                                    selectedRole,
                                    ride.id
                                )
                            }.await()
                    }
                }
                monto = paymentsRepository.getMonto( payments)
                if (selectedRole=="Conductor") {

                    ridesOtro.addAll(paymentsRepository.getRides("Pasajero"))
                    if (ridesOtro.isNotEmpty()) {
                        ridesOtro.forEach { ride ->
                            paymentsOtro[ride.id] =
                                async {
                                    paymentsRepository.getPayments(
                                        "Pasajero",
                                        ride.id
                                    )
                                }.await()
                        }
                    }
                }
                else{
                    ridesOtro.addAll(paymentsRepository.getRides("Conductor"))
                    if (ridesOtro.isNotEmpty()) {
                        ridesOtro.forEach { ride ->
                            paymentsOtro[ride.id] =
                                async {
                                    paymentsRepository.getPayments(
                                        "Conductor",
                                        ride.id
                                    )
                                }.await()
                        }
                    }
                }
                montoOtro = paymentsRepository.getMonto( paymentsOtro)
                isLoadingData = false
                paymentsRepository.clearLocalStorage()
                if (selectedRole == "Conductor") {
                    CacheManager.setRidesDriverPayments(rides)
                    CacheManager.setPaymentsDriver(payments)
                    CacheManager.setRidesRiderPayments(ridesOtro)
                    CacheManager.setPaymentsRider(paymentsOtro)
                    CacheManager.setMontoDriver(monto)
                    CacheManager.setMontoRider(montoOtro)
                    paymentsRepository.saveCache()
                } else {
                    CacheManager.setRidesRiderPayments(rides)
                    CacheManager.setPaymentsRider(payments)
                    CacheManager.setRidesDriverPayments(ridesOtro)
                    CacheManager.setPaymentsDriver(paymentsOtro)
                    CacheManager.setMontoRider(monto)
                    CacheManager.setMontoDriver(montoOtro)
                    paymentsRepository.saveCache()
                }
            }
        }
        else{
            viewModelScope.launch {
                isLoadingData = true
                if (selectedRole == "Conductor") {
                    val ridesCache = CacheManager.getRidesDriverPayments()
                    val paymentsCache = CacheManager.getPaymentsDriver()
                    val montoCache = CacheManager.getMontoDriver()
                    if (ridesCache.isEmpty()) {
                        async { paymentsRepository.readLocalStorage() }.await()
                        rides.addAll(CacheManager.getRidesDriverPayments())
                        payments.putAll(CacheManager.getPaymentsDriver())
                    }
                    else{
                        rides.addAll(ridesCache)
                        payments.putAll(paymentsCache)
                        monto = montoCache
                    }
                } else {
                    val ridesCache = CacheManager.getRidesRiderPayments()
                    val paymentsCache = CacheManager.getPaymentsRider()
                    if (ridesCache.isEmpty()) {
                        viewModelScope.launch { async { paymentsRepository.readLocalStorage() }.await() }
                        rides.addAll(CacheManager.getRidesRiderPayments())
                        payments.putAll(CacheManager.getPaymentsRider())
                    }
                    else{
                        rides.addAll(ridesCache)
                        payments.putAll(paymentsCache)
                    }
                }
                isLoadingData = false
            }
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
        loadData()
    }

    fun onRechazarPago(id:Int) {
        viewModelScope.launch {
            paymentsRepository.rechazarPago(id)
        }
        loadData()
    }

    fun onConfirmarPago(id:Int) {
        viewModelScope.launch {
            paymentsRepository.confirmarPago(id)
        }
        loadData()
    }
}