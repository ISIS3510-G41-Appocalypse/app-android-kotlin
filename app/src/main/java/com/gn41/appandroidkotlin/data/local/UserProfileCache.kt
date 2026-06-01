package com.gn41.appandroidkotlin.data.local

import com.gn41.appandroidkotlin.data.dto.auth.UserProfileDto

object UserProfileCache {

    private val cache =
        HashMap<Int, UserProfileDto>()

    fun get(userId: Int): UserProfileDto? =
        cache[userId]

    fun getCurrentUser(): UserProfileDto? =
        cache.values.firstOrNull()

    fun put(profile: UserProfileDto) {
        cache[profile.id] = profile
    }

    fun clear() {
        cache.clear()
    }
}