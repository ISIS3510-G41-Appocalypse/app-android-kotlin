package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.presentation.cache.TripMemoryCache
import com.gn41.appandroidkotlin.localStorage.LocalStorageManager
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.local.UserProfileCache
import com.gn41.appandroidkotlin.data.dto.auth.UserProfileDto

class SettingsViewModel(
    private val sessionManager: SessionManager,
    private val localStorageManager: LocalStorageManager
) : ViewModel() {

    var isDarkModeEnabled by mutableStateOf(sessionManager.isDarkModeEnabled())
        private set

    val appVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    fun toggleDarkMode(enabled: Boolean, onDarkModeChanged: (Boolean) -> Unit) {
        isDarkModeEnabled = enabled
        sessionManager.saveDarkModeEnabled(enabled)
        onDarkModeChanged(enabled)
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        sessionManager.clearToken()
        sessionManager.clearUserId()
        sessionManager.clearDriverId()

        UserProfileCache.clear()
        sessionManager.clearUserProfile()

        TripMemoryCache.clear()
        localStorageManager.clearTripState()

        onLogoutSuccess()
    }

    fun getCurrentUserProfile(): UserProfileDto? {
        val cachedUser = UserProfileCache.getCurrentUser()

        if (cachedUser != null) {
            return cachedUser
        }

        val storedUser = sessionManager.getUserProfile()

        if (storedUser != null) {
            UserProfileCache.put(storedUser)
        }

        return storedUser
    }
}
