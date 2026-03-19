package com.gn41.appandroidkotlin.data.services

import com.gn41.appandroidkotlin.data.services.auth.AuthApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {

    private const val BASE_URL = "https://cpayradzzfpyypefkvjg.supabase.co/"

    val authApi: AuthApi by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(AuthApi::class.java)
    }
}