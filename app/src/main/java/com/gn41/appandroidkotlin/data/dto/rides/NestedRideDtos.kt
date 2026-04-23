package com.gn41.appandroidkotlin.data.dto.rides

data class UserNestedDto(
    val id: Int = 0,
    val first_name: String? = null,
    val last_name: String? = null
)
data class DriverNestedDto(
    val id: Int = 0,
    val user_id: Int? = null,
    val rating: Double? = null,
    val cancellation_odds: Double? = null,
    val users: UserNestedDto? = null
)

data class VehicleNestedDto(
    val id: Int = 0,
    val brand: String? = null,
    val model: String? = null,
    val number_slots: Int? = null
)

data class ZoneNestedDto(
    val id: Int = 0,
    val name: String? = null
)
