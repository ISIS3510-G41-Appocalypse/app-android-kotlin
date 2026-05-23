package com.gn41.appandroidkotlin.data.local

import android.content.Context
import android.content.SharedPreferences

class RegisterDraftManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("register_draft", Context.MODE_PRIVATE)

    fun saveDraft(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: String,
        zoneId: Int?,
        vehicleLicensePlate: String,
        vehicleNumberSlots: Int?,
        vehicleBrand: String,
        vehicleModel: String,
        vehicleColor: String
    ) {
        sharedPreferences.edit().apply {
            putString("draft_first_name", firstName)
            putString("draft_last_name", lastName)
            putString("draft_email", email)
            putString("draft_password", password)
            putString("draft_role", role)
            if (zoneId != null) putInt("draft_zone_id", zoneId)
            else remove("draft_zone_id")
            putString("draft_vehicle_license_plate", vehicleLicensePlate)
            if (vehicleNumberSlots != null) putInt("draft_vehicle_number_slots", vehicleNumberSlots)
            else remove("draft_vehicle_number_slots")
            putString("draft_vehicle_brand", vehicleBrand)
            putString("draft_vehicle_model", vehicleModel)
            putString("draft_vehicle_color", vehicleColor)
            apply()
        }
    }

    fun getDraft(): RegisterDraft {
        val firstName = sharedPreferences.getString("draft_first_name", "") ?: ""
        val lastName = sharedPreferences.getString("draft_last_name", "") ?: ""
        val email = sharedPreferences.getString("draft_email", "") ?: ""
        val password = sharedPreferences.getString("draft_password", "") ?: ""
        val role = sharedPreferences.getString("draft_role", "") ?: ""
        val zoneId = if (sharedPreferences.contains("draft_zone_id")) sharedPreferences.getInt("draft_zone_id", -1).takeIf { it != -1 } else null
        val vehicleLicensePlate = sharedPreferences.getString("draft_vehicle_license_plate", "") ?: ""
        val vehicleNumberSlots = if (sharedPreferences.contains("draft_vehicle_number_slots")) sharedPreferences.getInt("draft_vehicle_number_slots", -1).takeIf { it != -1 } else null
        val vehicleBrand = sharedPreferences.getString("draft_vehicle_brand", "") ?: ""
        val vehicleModel = sharedPreferences.getString("draft_vehicle_model", "") ?: ""
        val vehicleColor = sharedPreferences.getString("draft_vehicle_color", "") ?: ""
        return RegisterDraft(
            firstName, lastName, email, password, role, zoneId,
            vehicleLicensePlate, vehicleNumberSlots, vehicleBrand, vehicleModel, vehicleColor
        )
    }

    fun clearDraft() {
        sharedPreferences.edit().clear().apply()
    }
}

data class RegisterDraft(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String,
    val zoneId: Int?,
    val vehicleLicensePlate: String,
    val vehicleNumberSlots: Int?,
    val vehicleBrand: String,
    val vehicleModel: String,
    val vehicleColor: String
)
