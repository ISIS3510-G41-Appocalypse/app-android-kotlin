package com.gn41.appandroidkotlin.presentation.cache

import com.gn41.appandroidkotlin.localStorage.RatingDraftDto

object RatingDraftCache {
    private val drafts = mutableMapOf<String, RatingDraftDto>()

    fun saveDraft(draft: RatingDraftDto) {
        drafts[buildKey(draft.authId, draft.rideId, draft.ratingType, draft.targetUserId)] = draft
    }

    fun getDraft(authId: String, rideId: Int, ratingType: String, targetUserId: Int): RatingDraftDto? {
        return drafts[buildKey(authId, rideId, ratingType, targetUserId)]
    }

    fun clearDraft(authId: String, rideId: Int, ratingType: String, targetUserId: Int) {
        drafts.remove(buildKey(authId, rideId, ratingType, targetUserId))
    }

    fun clearDraftsForRide(authId: String, rideId: Int, ratingType: String) {
        val prefix = "$authId-$rideId-$ratingType-"
        val keysToRemove = drafts.keys.filter { it.startsWith(prefix) }
        keysToRemove.forEach { drafts.remove(it) }
    }

    private fun buildKey(authId: String, rideId: Int, ratingType: String, targetUserId: Int): String {
        return "$authId-$rideId-$ratingType-$targetUserId"
    }
}

