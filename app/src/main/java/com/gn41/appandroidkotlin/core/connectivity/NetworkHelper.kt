package com.gn41.appandroidkotlin.core.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkHelper(private val context: Context) {

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        if (network == null) {
            Log.d("NetworkHelper", "[CHECK] activeNetwork=null capabilities=null internet=false validated=false result=false")
            return false
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities == null) {
            Log.d("NetworkHelper", "[CHECK] activeNetwork=$network capabilities=null internet=false validated=false result=false")
            return false
        }

        val hasInternetCapability =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val result = hasInternetCapability && isValidated

        Log.d(
            "NetworkHelper",
            "[CHECK] activeNetwork=$network internet=$hasInternetCapability validated=$isValidated result=$result"
        )

        return result
    }

    // Emite true cuando hay internet validado, false cuando no lo hay.
    fun observeNetworkChanges(): Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var delayedCheckJob: Job? = null

        fun sendCurrentState() {
            val hasInternet = isInternetAvailable()
            Log.d("NetworkHelper", "[EMIT] hasInternet=$hasInternet")
            trySend(hasInternet)
        }

        fun sendDelayedCheck() {
            delayedCheckJob?.cancel()
            delayedCheckJob = launch {
                delay(700)
                val hasInternet = isInternetAvailable()
                Log.d("NetworkHelper", "[DELAYED_EMIT] hasInternet=$hasInternet")
                trySend(hasInternet)
            }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("NetworkHelper", "[CALLBACK] onAvailable")
                sendCurrentState()
                sendDelayedCheck()
            }

            override fun onLost(network: Network) {
                Log.d("NetworkHelper", "[CALLBACK] onLost")
                sendCurrentState()
                sendDelayedCheck()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.d("NetworkHelper", "[CALLBACK] onCapabilitiesChanged")
                sendCurrentState()
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        // Emitir estado inicial
        sendCurrentState()

        awaitClose {
            delayedCheckJob?.cancel()
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}