package com.gn41.appandroidkotlin.localStorage

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.gn41.appandroidkotlin.cache.CacheManager
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveDriverTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRiderTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.TripReservationItemUiModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LocalStorageManager (private val context: Context) {

    private val gson = Gson()
    private val tripFileName = "trip_state.json"

    companion object {
        private const val TAG = "TripCache"
    }

    suspend fun saveFormState() = withContext(Dispatchers.IO) {
        val formState = CacheManager.getForm()

        val jsonString = gson.toJson(formState)

        val fileName = "form_state.json"

        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
        }
    }

    suspend fun readFormState():String = withContext(Dispatchers.IO) {
        val fileName = "form_state.json"
        val file = File(context.filesDir, fileName)
        if (file.exists()) {

            val jsonString = file.readText()

            val type = object : TypeToken<ArrayMap<String, String>>() {}.type

            val form = gson.fromJson<ArrayMap<String, String>>(jsonString, type)

            CacheManager.setForm(form)

            return@withContext "form_state.json cargado"
        }
        else
        {
            return@withContext "form_state.json no encontrado"
        }
    }

    suspend fun clearFormState() = withContext(Dispatchers.IO) {
        val fileName = "form_state.json"
        val file = File(context.filesDir, fileName)
        file.delete()
    }

    fun saveTripState(state: TripStorageDto) {
        try {
            val file = File(context.filesDir, tripFileName)
            val jsonString = gson.toJson(state)
            FileOutputStream(file).use { stream ->
                stream.write(jsonString.toByteArray())
            }
            Log.d(TAG, "file save ok authIdPresent=true rider=${state.activeRiderTrips.size} driverRes=${state.activeDriverTrip?.reservations?.size ?: 0}")
        } catch (e: Exception) {
            Log.e(TAG, "file save error", e)
        }
    }

    fun readTripState(authId: String): TripStorageDto? {
        val file = File(context.filesDir, tripFileName)
        if (!file.exists()) {
            Log.d(TAG, "file read miss: not found")
            return null
        }

        return try {
            val jsonString = file.readText()
            val state = parseTripState(jsonString)
            if (state?.authId == authId) {
                Log.d(TAG, "file read ok authIdPresent=true rider=${state.activeRiderTrips.size} driverRes=${state.activeDriverTrip?.reservations?.size ?: 0}")
                state
            } else {
                Log.d(TAG, "file read ignored: auth mismatch")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "file read error", e)
            null
        }
    }

    fun clearTripState() {
        val file = File(context.filesDir, tripFileName)
        if (file.exists()) {
            file.delete()
            Log.d(TAG, "file cleared")
        } else {
            Log.d(TAG, "file clear skipped: not found")
        }
    }

    private fun parseTripState(jsonString: String): TripStorageDto? {
        val root = JsonParser.parseString(jsonString).takeIf { it.isJsonObject }?.asJsonObject ?: return null

        val riderTrips = root.getAsJsonArrayOrEmpty("activeRiderTrips")
            .mapNotNull { element ->
                element.takeIf { it.isJsonObject }?.asJsonObject?.toActiveRiderTrip()
            }

        val driverTrip = root.getAsJsonObjectOrNull("activeDriverTrip")?.toActiveDriverTrip()

        return TripStorageDto(
            authId = root.getStringOrNull("authId") ?: return null,
            currentUserId = root.getIntOrNull("currentUserId"),
            currentRideId = root.getIntOrNull("currentRideId"),
            activeRiderTrips = riderTrips,
            activeDriverTrip = driverTrip,
            savedAt = root.getLongOrNull("savedAt") ?: 0L
        )
    }

    private fun JsonObject.toActiveRiderTrip(): ActiveRiderTripUiModel? {
        val status = getStringOrNull("status") ?: return null
        val rideStatus = getStringOrNull("rideStatus") ?: return null

        return ActiveRiderTripUiModel(
            reservationId = getIntOrNull("reservationId") ?: return null,
            rideId = getIntOrNull("rideId") ?: return null,
            source = getStringOrNull("source") ?: return null,
            destination = getStringOrNull("destination") ?: return null,
            status = status,
            rideStatus = rideStatus,
            departureTime = getStringOrNull("departureTime") ?: "",
            driverName = getStringOrNull("driverName")?.takeIf { it.isNotBlank() } ?: "Conductor",
            departureDate = getStringOrNull("departureDate")?.takeIf { it.isNotBlank() } ?: "Por definir",
            canCancelReservation = getBooleanOrNull("canCancelReservation") ?: false,
            showCancelButton = getBooleanOrNull("showCancelButton") ?: true,
            cancelDisabledReason = getStringOrNull("cancelDisabledReason")
        )
    }

    private fun JsonObject.toActiveDriverTrip(): ActiveDriverTripUiModel? {
        val reservations = getAsJsonArrayOrEmpty("reservations")
            .mapNotNull { element ->
                element.takeIf { it.isJsonObject }?.asJsonObject?.toTripReservationItem()
            }

        val acceptedReservations = getIntOrNull("acceptedReservations")
            ?: reservations.count { reservation ->
                reservation.status.equals("ACEPTADA", ignoreCase = true) || reservation.status.equals("EN_CURSO", ignoreCase = true)
            }

        val totalSeats = getIntOrNull("totalSeats") ?: 0

        return ActiveDriverTripUiModel(
            rideId = getIntOrNull("rideId") ?: return null,
            source = getStringOrNull("source") ?: return null,
            destination = getStringOrNull("destination") ?: return null,
            status = getStringOrNull("status") ?: return null,
            departureTime = getStringOrNull("departureTime") ?: "",
            reservationsCount = getIntOrNull("reservationsCount") ?: reservations.size,
            totalSeats = totalSeats,
            acceptedReservations = acceptedReservations,
            availableSeats = getIntOrNull("availableSeats") ?: (totalSeats - acceptedReservations).coerceAtLeast(0),
            reservations = reservations,
            departureDate = getStringOrNull("departureDate")?.takeIf { it.isNotBlank() } ?: "Por definir"
        )
    }

    private fun JsonObject.toTripReservationItem(): TripReservationItemUiModel? {
        return TripReservationItemUiModel(
            id = getIntOrNull("id") ?: return null,
            riderName = getStringOrNull("riderName")?.takeIf { it.isNotBlank() } ?: "Rider",
            status = getStringOrNull("status") ?: return null,
            cancellationOdds = getDoubleOrNull("cancellationOdds"),
            paymentMethod = getStringOrNull("paymentMethod")?.takeIf { it.isNotBlank() } ?: "Por definir"
        )
    }

    private fun JsonObject.getAsJsonObjectOrNull(memberName: String): JsonObject? {
        val value = get(memberName) ?: return null
        return value.takeIf { it.isJsonObject }?.asJsonObject
    }

    private fun JsonObject.getAsJsonArrayOrEmpty(memberName: String): JsonArray {
        val value = get(memberName)
        return if (value != null && value.isJsonArray) value.asJsonArray else JsonArray()
    }

    private fun JsonObject.getStringOrNull(memberName: String): String? {
        val value = get(memberName) ?: return null
        return value.toPrimitiveOrNull()?.takeUnless { it.isJsonNull }?.asString
    }

    private fun JsonObject.getIntOrNull(memberName: String): Int? {
        val value = get(memberName) ?: return null
        return value.toPrimitiveOrNull()?.takeUnless { it.isJsonNull }?.asInt
    }

    private fun JsonObject.getLongOrNull(memberName: String): Long? {
        val value = get(memberName) ?: return null
        return value.toPrimitiveOrNull()?.takeUnless { it.isJsonNull }?.asLong
    }

    private fun JsonObject.getDoubleOrNull(memberName: String): Double? {
        val value = get(memberName) ?: return null
        return value.toPrimitiveOrNull()?.takeUnless { it.isJsonNull }?.asDouble
    }

    private fun JsonObject.getBooleanOrNull(memberName: String): Boolean? {
        val value = get(memberName) ?: return null
        return value.toPrimitiveOrNull()?.takeUnless { it.isJsonNull }?.asBoolean
    }

    private fun JsonElement.toPrimitiveOrNull() = takeIf { it.isJsonPrimitive }?.asJsonPrimitive
}