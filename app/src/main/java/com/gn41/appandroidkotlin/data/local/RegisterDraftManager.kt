package com.gn41.appandroidkotlin.data.local

import android.content.Context
import android.content.SharedPreferences

class RegisterDraftManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("register_draft", Context.MODE_PRIVATE)

    fun saveDraft(name: String, email: String, password: String) {
        sharedPreferences.edit().apply {
            putString("draft_name", name)
            putString("draft_email", email)
            putString("draft_password", password)
            apply()
        }
    }

    fun getDraft(): Triple<String, String, String> {
        val name = sharedPreferences.getString("draft_name", "") ?: ""
        val email = sharedPreferences.getString("draft_email", "") ?: ""
        val password = sharedPreferences.getString("draft_password", "") ?: ""
        return Triple(name, email, password)
    }

    fun clearDraft() {
        sharedPreferences.edit().clear().apply()
    }
}