package com.gn41.appandroidkotlin.data.repositories

import com.gn41.appandroidkotlin.data.dto.location.UserSharedLocationDto
import com.gn41.appandroidkotlin.data.local.SessionManager
import com.gn41.appandroidkotlin.data.services.location.LocationService
import com.gn41.appandroidkotlin.domain.UserSharedLocation
import org.json.JSONArray
import org.json.JSONObject

class LocationRepositoryImpl(
    private val locationService: LocationService,
    private val sessionManager: SessionManager
) : LocationRepository {

    override suspend fun saveLocation(
        location: UserSharedLocation,
        token: String
    ): Boolean {
        val dto = UserSharedLocationDto(
            id = location.id,
            user_id = location.userId,
            ride_id = location.rideId,
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = location.timestamp,
            is_sharing_enabled = location.isSharingEnabled
        )

        return locationService.insertUserLocation(dto, token)
    }

    override suspend fun getLocationsByRide(
        rideId: Int,
        token: String
    ): LocationResult {
        return try {
            val locations = locationService.getLocationsByRide(rideId, token).map { dto ->
                UserSharedLocation(
                    id = dto.id,
                    userId = dto.user_id,
                    rideId = dto.ride_id,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    timestamp = dto.timestamp,
                    isSharingEnabled = dto.is_sharing_enabled
                )
            }

            sessionManager.saveCachedRideLocations(
                rideId = rideId,
                locationsJson = locationsToJson(locations)
            )

            LocationResult(
                locations = locations,
                isFromCache = false
            )
        } catch (_: Exception) {
            val cachedLocations = locationsFromJson(
                sessionManager.getCachedRideLocations(rideId)
            )

            LocationResult(
                locations = cachedLocations,
                isFromCache = true,
                message = if (cachedLocations.isNotEmpty()) {
                    "No pudimos actualizar el mapa. Mostramos la última ubicación conocida."
                } else {
                    "No pudimos actualizar el mapa y todavía no hay ubicaciones guardadas."
                }
            )
        }
    }

    private fun locationsToJson(locations: List<UserSharedLocation>): String {
        val array = JSONArray()

        locations.forEach { location ->
            val item = JSONObject()
            item.put("id", location.id)
            item.put("userId", location.userId)
            item.put("rideId", location.rideId)
            item.put("latitude", location.latitude)
            item.put("longitude", location.longitude)
            item.put("timestamp", location.timestamp)
            item.put("isSharingEnabled", location.isSharingEnabled)
            array.put(item)
        }

        return array.toString()
    }

    private fun locationsFromJson(json: String): List<UserSharedLocation> {
        if (json.isBlank()) return emptyList()

        return try {
            val array = JSONArray(json)
            val locations = mutableListOf<UserSharedLocation>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                locations.add(
                    UserSharedLocation(
                        id = if (item.isNull("id")) null else item.getInt("id"),
                        userId = item.getInt("userId"),
                        rideId = item.getInt("rideId"),
                        latitude = item.getDouble("latitude"),
                        longitude = item.getDouble("longitude"),
                        timestamp = item.getString("timestamp"),
                        isSharingEnabled = item.getBoolean("isSharingEnabled")
                    )
                )
            }

            locations
        } catch (_: Exception) {
            emptyList()
        }
    }
}