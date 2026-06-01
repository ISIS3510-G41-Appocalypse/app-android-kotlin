package com.gn41.appandroidkotlin.data.local

import android.content.Context
import com.gn41.appandroidkotlin.data.dto.auth.UserProfileDto

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

    fun saveDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode_enabled", enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode_enabled", true)
    }

    fun getSkippedDriverRatingRideIds(authId: String): Set<Int> {
        if (authId.isBlank()) return emptySet()
        return sharedPreferences
            .getStringSet("skipped_driver_rating_$authId", emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun getSkippedRiderRatingRideIds(authId: String): Set<Int> {
        if (authId.isBlank()) return emptySet()
        return sharedPreferences
            .getStringSet("skipped_rider_rating_$authId", emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun addSkippedDriverRatingRideId(authId: String, rideId: Int) {
        if (authId.isBlank()) return
        val key = "skipped_driver_rating_$authId"
        val current = sharedPreferences.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        current.add(rideId.toString())
        sharedPreferences.edit().putStringSet(key, current).apply()
    }

    fun addSkippedRiderRatingRideId(authId: String, rideId: Int) {
        if (authId.isBlank()) return
        val key = "skipped_rider_rating_$authId"
        val current = sharedPreferences.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        current.add(rideId.toString())
        sharedPreferences.edit().putStringSet(key, current).apply()
    }

    fun getPendingDriverRatingRideIds(authId: String): Set<Int> {
        if (authId.isBlank()) return emptySet()
        return sharedPreferences
            .getStringSet("pending_driver_rating_$authId", emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun getPendingRiderRatingRideIds(authId: String): Set<Int> {
        if (authId.isBlank()) return emptySet()
        return sharedPreferences
            .getStringSet("pending_rider_rating_$authId", emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun addPendingDriverRatingRideId(authId: String, rideId: Int) {
        if (authId.isBlank()) return
        val key = "pending_driver_rating_$authId"
        val current = sharedPreferences.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        current.add(rideId.toString())
        sharedPreferences.edit().putStringSet(key, current).apply()
    }

    fun addPendingRiderRatingRideId(authId: String, rideId: Int) {
        if (authId.isBlank()) return
        val key = "pending_rider_rating_$authId"
        val current = sharedPreferences.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        current.add(rideId.toString())
        sharedPreferences.edit().putStringSet(key, current).apply()
    }

    fun removePendingDriverRatingRideId(authId: String, rideId: Int) {
        if (authId.isBlank()) return
        val key = "pending_driver_rating_$authId"
        val current = sharedPreferences.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        current.remove(rideId.toString())
        sharedPreferences.edit().putStringSet(key, current).apply()
    }

    fun removePendingRiderRatingRideId(authId: String, rideId: Int) {
        if (authId.isBlank()) return
        val key = "pending_rider_rating_$authId"
        val current = sharedPreferences.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        current.remove(rideId.toString())
        sharedPreferences.edit().putStringSet(key, current).apply()
    }



    fun saveUserProfile(profile: UserProfileDto) {

        sharedPreferences.edit()
            .putInt("profile_id", profile.id)
            .putString("profile_first_name", profile.first_name)
            .putString("profile_last_name", profile.last_name)
            .putInt("profile_zone_id", profile.zone_id)
            .putString("profile_auth_id", profile.auth_id)
            .apply()
    }

    fun getUserProfile(): UserProfileDto? {

        val id = sharedPreferences.getInt("profile_id", -1)

        if (id == -1) {
            return null
        }

        return UserProfileDto(
            id = id,
            first_name = sharedPreferences.getString(
                "profile_first_name",
                ""
            ) ?: "",
            last_name = sharedPreferences.getString(
                "profile_last_name",
                ""
            ) ?: "",
            zone_id = sharedPreferences.getInt(
                "profile_zone_id",
                -1
            ),
            auth_id = sharedPreferences.getString(
                "profile_auth_id",
                ""
            ) ?: ""
        )
    }

    fun clearUserProfile() {

        sharedPreferences.edit()
            .remove("profile_id")
            .remove("profile_first_name")
            .remove("profile_last_name")
            .remove("profile_zone_id")
            .remove("profile_auth_id")
            .apply()
    }
}
