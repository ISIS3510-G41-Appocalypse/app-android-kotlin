package com.gn41.appandroidkotlin.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gn41.appandroidkotlin.data.dto.createRide.ActiveRideDto
import com.gn41.appandroidkotlin.data.dto.createRide.RideUserDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.repositories.RideRepository
import com.gn41.appandroidkotlin.data.repositories.VehicleRepository
import com.gn41.appandroidkotlin.data.repositories.ZoneRepository
import kotlinx.coroutines.launch

class ActiveRideViewModel(
    private val rideRepository: RideRepository
) : ViewModel() {

    var ride by mutableStateOf<ActiveRideDto?>(null)
        private set

    var rideUsers by mutableStateOf<List<RideUserDto>?>(null)
        private set


    init{
        loadingData()
    }

    private fun loadingData(){
        viewModelScope.launch {
            ride = rideRepository.getActiveRide()

            if (ride != null){
                rideUsers = rideRepository.getRideUsers(ride!!.id)
                Log.d("rideUsers", rideUsers.toString())
            }
        }
    }

    fun onCancelarViaje(id: Int){
        viewModelScope.launch {
            rideRepository.cancelRide(id)

        }
    }
}