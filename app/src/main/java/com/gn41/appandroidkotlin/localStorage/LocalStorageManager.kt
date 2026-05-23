package com.gn41.appandroidkotlin.localStorage

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.gn41.appandroidkotlin.cache.CacheManager
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
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

    private val ratingDraftsFileName = "rating_drafts.json"
    private val pendingRatingsFileName = "pending_ratings.json"

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

    suspend fun savePaymentsState() = withContext(Dispatchers.IO) {
        val ridesRiderPayments = CacheManager.getRidesRiderPayments()

        val ridesDriverPayments = CacheManager.getRidesDriverPayments()

        val paymentsRider = CacheManager.getPaymentsRider()

        val paymentsDriver = CacheManager.getPaymentsDriver()

        val jsonString1 = gson.toJson(ridesRiderPayments)

        val jsonString2 = gson.toJson(ridesDriverPayments)

        val jsonString3 = gson.toJson(paymentsRider)

        val jsonString4 = gson.toJson(paymentsDriver)

        var fileName = "rides_rider_payments.json"

        var file = File(context.filesDir, fileName)

        FileOutputStream(file).use { stream ->
            stream.write(jsonString1.toByteArray())
        }

        fileName = "rides_driver_payments.json"

        file = File(context.filesDir, fileName)

        FileOutputStream(file).use { stream ->
            stream.write(jsonString2.toByteArray())
        }

        fileName = "payments_rider.json"

        file = File(context.filesDir, fileName)

        FileOutputStream(file).use { stream ->
            stream.write(jsonString3.toByteArray())
        }

        fileName = "payments_driver.json"

        file = File(context.filesDir, fileName)

        FileOutputStream(file).use { stream ->
            stream.write(jsonString4.toByteArray())
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

    suspend fun readPaymentsState():String = withContext(Dispatchers.IO) {
        var fileName = "rides_rider_payments.json"
        var file = File(context.filesDir, fileName)
        if (file.exists()) {

            var jsonString = file.readText()

            var type = object : TypeToken<MutableList<RidePaymentDto>>() {}.type

            val ridesRiderPayments = gson.fromJson<MutableList<RidePaymentDto>>(jsonString, type)

            CacheManager.setRidesRiderPayments(ridesRiderPayments)

            fileName = "rides_driver_payments.json"
            file = File(context.filesDir, fileName)

            if (file.exists()) {
                jsonString = file.readText()
                type = object : TypeToken<MutableList<RidePaymentDto>>() {}.type
                val ridesDriverPayments = gson.fromJson<MutableList<RidePaymentDto>>(jsonString, type)
                CacheManager.setRidesDriverPayments(ridesDriverPayments)

                fileName = "payments_rider.json"
                file = File(context.filesDir, fileName)
                if (file.exists()) {
                    jsonString = file.readText()
                    type = object : TypeToken<MutableMap<Int, List<PaymentDto>>>() {}.type
                    val paymentsRider = gson.fromJson<MutableMap<Int, List<PaymentDto>>>(jsonString, type)
                    CacheManager.setPaymentsRider(paymentsRider)

                    fileName = "payments_driver.json"
                    file = File(context.filesDir, fileName)
                    if (file.exists()) {
                        jsonString = file.readText()
                        type = object : TypeToken<MutableMap<Int, List<PaymentDto>>>() {}.type
                        val paymentsDriver = gson.fromJson<MutableMap<Int, List<PaymentDto>>>(jsonString, type)
                        CacheManager.setPaymentsDriver(paymentsDriver)
                        return@withContext "payments state cargados"
                    }
                    else{
                        return@withContext "payments state no encontrados"
                    }
                }
                else{
                    return@withContext "payments state no encontrados"
                }
            }
            else
            {
                return@withContext "payments state no encontrados"
            }

        }
        else
        {
            return@withContext "payments state no encontrados"
        }
    }

    suspend fun clearFormState() = withContext(Dispatchers.IO) {
        val fileName = "form_state.json"
        val file = File(context.filesDir, fileName)
        file.delete()
    }

    suspend fun clearPaymentsState() = withContext(Dispatchers.IO) {
        var fileName = "rides_rider_payments.json"
        var file = File(context.filesDir, fileName)
        file.delete()

        fileName = "rides_driver_payments.json"
        file = File(context.filesDir, fileName)
        file.delete()

        fileName = "payments_rider.json"
        file = File(context.filesDir, fileName)
        file.delete()

        fileName = "payments_driver.json"
        file = File(context.filesDir, fileName)
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

    fun saveRatingDraft(draft: RatingDraftDto) {
        try {
            val drafts = readAllRatingDrafts().toMutableList()
            val index = drafts.indexOfFirst {
                it.authId == draft.authId &&
                    it.rideId == draft.rideId &&
                    it.ratingType == draft.ratingType &&
                    it.targetUserId == draft.targetUserId
            }
            if (index >= 0) {
                drafts[index] = draft
            } else {
                drafts.add(draft)
            }
            writeAllRatingDrafts(drafts)
        } catch (e: Exception) {
            Log.e(TAG, "rating draft save error", e)
        }
    }

    fun readRatingDraft(authId: String, rideId: Int, ratingType: String, targetUserId: Int): RatingDraftDto? {
        return try {
            readAllRatingDrafts().firstOrNull {
                it.authId == authId &&
                    it.rideId == rideId &&
                    it.ratingType == ratingType &&
                    it.targetUserId == targetUserId
            }
        } catch (e: Exception) {
            Log.e(TAG, "rating draft read error", e)
            null
        }
    }

    fun clearRatingDraft(authId: String, rideId: Int, ratingType: String, targetUserId: Int) {
        try {
            val updatedDrafts = readAllRatingDrafts().filterNot {
                it.authId == authId &&
                    it.rideId == rideId &&
                    it.ratingType == ratingType &&
                    it.targetUserId == targetUserId
            }
            writeAllRatingDrafts(updatedDrafts)
        } catch (e: Exception) {
            Log.e(TAG, "rating draft clear error", e)
        }
    }

    fun clearRatingDraftsForRide(authId: String, rideId: Int, ratingType: String) {
        try {
            val updatedDrafts = readAllRatingDrafts().filterNot {
                it.authId == authId &&
                    it.rideId == rideId &&
                    it.ratingType == ratingType
            }
            writeAllRatingDrafts(updatedDrafts)
        } catch (e: Exception) {
            Log.e(TAG, "rating drafts clear by ride error", e)
        }
    }

    fun savePendingRating(pendingRating: PendingRatingDto) {
        try {
            val ratings = readAllPendingRatings().toMutableList()
            val index = ratings.indexOfFirst {
                it.authId == pendingRating.authId &&
                    it.rideId == pendingRating.rideId &&
                    it.ratingType == pendingRating.ratingType
            }
            if (index >= 0) {
                ratings[index] = pendingRating
            } else {
                ratings.add(pendingRating)
            }
            writeAllPendingRatings(ratings)
            Log.d(TAG, "pending rating saved authIdPresent=true rideId=${pendingRating.rideId} type=${pendingRating.ratingType}")
        } catch (e: Exception) {
            Log.e(TAG, "pending rating save error", e)
        }
    }

    fun readPendingRatings(authId: String): List<PendingRatingDto> {
        return try {
            readAllPendingRatings().filter { it.authId == authId }
        } catch (e: Exception) {
            Log.e(TAG, "pending ratings read error", e)
            emptyList()
        }
    }

    fun clearPendingRating(authId: String, rideId: Int, ratingType: String) {
        try {
            val updated = readAllPendingRatings().filterNot {
                it.authId == authId && it.rideId == rideId && it.ratingType == ratingType
            }
            writeAllPendingRatings(updated)
            Log.d(TAG, "pending rating cleared authIdPresent=true rideId=$rideId type=$ratingType")
        } catch (e: Exception) {
            Log.e(TAG, "pending rating clear error", e)
        }
    }

    fun clearOldRiderPendingRatings(authId: String, keepRideId: Int? = null) {
        try {
            val all = readAllPendingRatings()
            val removed = all.count { pending ->
                pending.authId == authId &&
                    pending.ratingType == "driver" &&
                    (keepRideId == null || pending.rideId != keepRideId)
            }
            val updated = all.filterNot { pending ->
                pending.authId == authId &&
                    pending.ratingType == "driver" &&
                    (keepRideId == null || pending.rideId != keepRideId)
            }
            writeAllPendingRatings(updated)
            Log.d(TAG, "clear old rider pending ratings keepRideId=$keepRideId removed=$removed")
        } catch (e: Exception) {
            Log.e(TAG, "clear old rider pending ratings error", e)
        }
    }

    fun clearPendingRatingsForRide(authId: String, rideId: Int) {
        try {
            val updated = readAllPendingRatings().filterNot {
                it.authId == authId && it.rideId == rideId
            }
            writeAllPendingRatings(updated)
            Log.d(TAG, "pending ratings for ride cleared authIdPresent=true rideId=$rideId")
        } catch (e: Exception) {
            Log.e(TAG, "pending ratings for ride clear error", e)
        }
    }

    fun removeRiderFromPendingRating(authId: String, rideId: Int, riderId: Int): Boolean {
        return try {
            val ratings = readAllPendingRatings().toMutableList()
            val index = ratings.indexOfFirst {
                it.authId == authId &&
                    it.rideId == rideId &&
                    it.ratingType == "rider"
            }

            if (index < 0) {
                false
            } else {
                val pending = ratings[index]
                val updatedRiders = pending.ridersToRate.orEmpty().filterNot { it.riderId == riderId }

                if (updatedRiders.isEmpty()) {
                    ratings.removeAt(index)
                    writeAllPendingRatings(ratings)
                    Log.d(TAG, "pending rider local cleared rideId=$rideId because no riders left")
                    true
                } else {
                    ratings[index] = pending.copy(
                        ridersToRate = updatedRiders,
                        updatedAt = System.currentTimeMillis()
                    )
                    writeAllPendingRatings(ratings)
                    Log.d(
                        TAG,
                        "pending rider removed from local rideId=$rideId riderId=$riderId remaining=${updatedRiders.size}"
                    )
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "pending rider remove from local error", e)
            false
        }
    }

    private fun readAllRatingDrafts(): List<RatingDraftDto> {
        val file = File(context.filesDir, ratingDraftsFileName)
        if (!file.exists()) return emptyList()

        return try {
            val jsonString = file.readText()
            val type = object : TypeToken<List<RatingDraftDto>>() {}.type
            gson.fromJson<List<RatingDraftDto>>(jsonString, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeAllRatingDrafts(drafts: List<RatingDraftDto>) {
        val file = File(context.filesDir, ratingDraftsFileName)
        val jsonString = gson.toJson(drafts)
        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
        }
    }

    private fun readAllPendingRatings(): List<PendingRatingDto> {
        val file = File(context.filesDir, pendingRatingsFileName)
        if (!file.exists()) return emptyList()

        return try {
            val jsonString = file.readText()
            val type = object : TypeToken<List<PendingRatingDto>>() {}.type
            gson.fromJson<List<PendingRatingDto>>(jsonString, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeAllPendingRatings(pendingRatings: List<PendingRatingDto>) {
        val file = File(context.filesDir, pendingRatingsFileName)
        val jsonString = gson.toJson(pendingRatings)
        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
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
            riderRating = getDoubleOrNull("riderRating"),
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