package com.gn41.appandroidkotlin.data.dto.trips

data class TripUserDto(
    val id: Int,
    val auth_id: String? = null
)

data class TripRiderDto(
    val id: Int,
    val user_id: Int
)

data class TripDriverDto(
    val id: Int,
    val user_id: Int
)

data class TripUserNestedDto(
    val first_name: String? = null,
    val last_name: String? = null
)

data class TripRiderNestedDto(
    val id: Int,
    val cancellation_odds: Double,
    val users: TripUserNestedDto? = null
)

data class TripRideNestedDto(
    val id: Int,
    val source: String,
    val destination: String,
    val state: String,
    val departure_time: String,
    val date: String
)

data class TripReservationDto(
    val id: Int,
    val ride_id: Int,
    val rider_id: Int,
    val state: String,
    val rides: TripRideNestedDto? = null,
    val riders: TripRiderNestedDto? = null
)

data class TripRideDto(
    val id: Int,
    val source: String,
    val destination: String,
    val state: String,
    val departure_time: String,
    val date: String,
    val vehicles: TripVehicleNestedDto? = null
)

data class TripVehicleNestedDto(
    val number_slots: Int? = null
)

