package com.gn41.appandroidkotlin.cache

import android.util.ArrayMap

object CacheManager {
    private val cacheFormState : ArrayMap<String,String> = ArrayMap()

    fun putFormState(key: String, value: String) {
        cacheFormState[key] = value
    }

    fun getFormState(key: String): String? {
        return cacheFormState[key]
    }

    fun containsKeyFormState(key: String): Boolean {
        return cacheFormState.containsKey(key)
    }

    fun clearFormState() {
        cacheFormState.clear()
    }

    fun getForm(): ArrayMap<String,String>? {
        return cacheFormState
    }

    fun setForm(form: ArrayMap<String,String>) {
        cacheFormState.clear()
        cacheFormState.putAll(form)
    }

}