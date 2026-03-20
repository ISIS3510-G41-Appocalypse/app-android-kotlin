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
}