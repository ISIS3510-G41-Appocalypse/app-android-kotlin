package com.gn41.appandroidkotlin.data.services.auth

import android.util.Log
import com.gn41.appandroidkotlin.BuildConfig
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.CreateCompleteUserResponseDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginRequestDto
import com.gn41.appandroidkotlin.data.dto.auth.LoginResponseDto
import com.gn41.appandroidkotlin.data.dto.auth.UserProfileDto
import com.gn41.appandroidkotlin.data.services.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthService {

    private val authApi = SupabaseClient.authApi

    suspend fun login(
        email: String,
        password: String
    ): LoginResponseDto? = withContext(Dispatchers.IO) {

        return@withContext try {

            val loginRequest = LoginRequestDto(
                email = email,
                password = password
            )

            val response = authApi.login(
                apiKey = BuildConfig.SUPABASE_KEY,
                request = loginRequest
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()
            } else {
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createCompleteUser(
        request: CreateCompleteUserRequestDto
    ): CreateCompleteUserResponseDto = withContext(Dispatchers.IO) {

        return@withContext try {

            val response = authApi.createCompleteUser(
                token = "Bearer ${BuildConfig.SUPABASE_KEY}",
                apiKey = BuildConfig.SUPABASE_KEY,
                request = request
            )

            if (response.isSuccessful && response.body() != null) {

                response.body()!!

            } else {

                val errorBody = response.errorBody()?.string()
                Log.e("AuthService", "Raw error body: $errorBody")

                when {

                    errorBody?.contains("USER_ALREADY_EXISTS") == true -> {
                        CreateCompleteUserResponseDto(
                            success = false,
                            error_code = "USER_ALREADY_EXISTS",
                            error = "Ya existe una cuenta con este correo."
                        )
                    }

                    errorBody?.contains("VEHICLE_REQUIRED") == true -> {
                        CreateCompleteUserResponseDto(
                            success = false,
                            error_code = "VEHICLE_REQUIRED",
                            error = "Debes ingresar la información del vehículo."
                        )
                    }

                    errorBody?.contains("INVALID_ROLE") == true -> {
                        CreateCompleteUserResponseDto(
                            success = false,
                            error_code = "INVALID_ROLE",
                            error = "El rol seleccionado no es válido."
                        )
                    }

                    errorBody?.contains("MISSING_REQUIRED_FIELDS") == true -> {
                        CreateCompleteUserResponseDto(
                            success = false,
                            error_code = "MISSING_REQUIRED_FIELDS",
                            error = "Faltan campos obligatorios."
                        )
                    }

                    else -> {
                        CreateCompleteUserResponseDto(
                            success = false,
                            error_code = "UNKNOWN_SERVER_ERROR",
                            error = "Ocurrió un error desconocido. Repórtalo para poder solucionarlo."
                        )
                    }
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()

            CreateCompleteUserResponseDto(
                success = false,
                error_code = "NETWORK_ERROR",
                error = "No se pudo conectar con el servidor. Revisa tu conexión."
            )
        }
    }



    suspend fun getUserProfile(
        authId: String,
        token: String
    ): UserProfileDto? = withContext(Dispatchers.IO) {

        return@withContext try {

            val response = authApi.getUserProfile(
                token = "Bearer $token",
                apiKey = BuildConfig.SUPABASE_KEY,
                authId = "eq.$authId"
            )

            Log.d(
                "PROFILE_RESPONSE",
                "success=${response.isSuccessful}"
            )

            Log.d(
                "PROFILE_RESPONSE",
                "body=${response.body()}"
            )

            if (
                response.isSuccessful &&
                !response.body().isNullOrEmpty()
            ) {

                Log.d(
                    "PROFILE_RESPONSE",
                    "first=${response.body()!!.first()}"
                )

                response.body()!!.first()

            } else {

                Log.e(
                    "PROFILE_RESPONSE",
                    "error=${response.errorBody()?.string()}"
                )

                null
            }

        } catch (e: Exception) {

            Log.e(
                "PROFILE_RESPONSE",
                "exception",
                e
            )

            null
        }
    }
}