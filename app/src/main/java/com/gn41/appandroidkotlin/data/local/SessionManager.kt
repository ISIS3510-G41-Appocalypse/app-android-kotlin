package com.gn41.appandroidkotlin.data.local

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("happyride_session", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString("session_token", token)
            .apply()
    }

    fun getToken(): String {
        return sharedPreferences.getString("session_token", "") ?: ""
    }

    fun clearToken() {
        sharedPreferences.edit()
            .remove("session_token")
            .apply()
    }

    fun saveUserId(id: String) {
        sharedPreferences.edit()
            .putString("session_user_id", id)
            .apply()
    }

    fun getUserId(): String {
        return sharedPreferences.getString("session_user_id", "") ?: ""
    }

    fun clearUserId() {
        sharedPreferences.edit()
            .remove("session_user_id")
            .apply()
    }

    fun saveDriverId(id: Int) {
        sharedPreferences.edit()
            .putInt("session_driver_id", id)
            .apply()
    }

    fun getDriverId(): Int {
        return sharedPreferences.getInt("session_driver_id", -1)
    }

    fun saveCurrentRideId(id: Int) {
        sharedPreferences.edit()
            .putInt("session_current_ride_id", id)
            .apply()
    }

    fun getCurrentRideId(): Int {
        return sharedPreferences.getInt("session_current_ride_id", -1)
    }

    fun clearDriverId() {
        sharedPreferences.edit()
            .remove("session_driver_id")
            .apply()
    }

    fun saveLocationSharingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("location_sharing_enabled", enabled).apply()
    }

    fun isLocationSharingEnabled(): Boolean {
        return sharedPreferences.getBoolean("location_sharing_enabled", false)
    }


    fun saveCachedRideLocations(rideId: Int, locationsJson: String) {
        sharedPreferences.edit()
            .putString("cached_ride_locations_$rideId", locationsJson)
            .apply()
    }

    fun getCachedRideLocations(rideId: Int): String {
        return sharedPreferences.getString("cached_ride_locations_$rideId", "") ?: ""
    }
}