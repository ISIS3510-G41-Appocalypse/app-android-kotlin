package com.gn41.appandroidkotlin.localStorage

import android.content.Context
import android.util.ArrayMap
import com.gn41.appandroidkotlin.cache.CacheManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LocalStorageManager (private val context: Context) {

    private val gson = Gson()
    private val tripFileName = "trip_state.json"

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
        val file = File(context.filesDir, tripFileName)
        val jsonString = gson.toJson(state)
        FileOutputStream(file).use { stream ->
            stream.write(jsonString.toByteArray())
        }
    }

    fun readTripState(authId: String): TripStorageDto? {
        val file = File(context.filesDir, tripFileName)
        if (!file.exists()) {
            return null
        }

        return try {
            val jsonString = file.readText()
            val state = gson.fromJson(jsonString, TripStorageDto::class.java)
            if (state?.authId == authId) state else null
        } catch (_: Exception) {
            null
        }
    }

    fun clearTripState() {
        val file = File(context.filesDir, tripFileName)
        if (file.exists()) {
            file.delete()
        }
    }
}