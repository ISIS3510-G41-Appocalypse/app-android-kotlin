package com.gn41.appandroidkotlin.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEvents {
    private val _onSessionExpired = MutableSharedFlow<Unit>()
    val onSessionExpired = _onSessionExpired.asSharedFlow()

    suspend fun emitSessionExpired() {
        _onSessionExpired.emit(Unit)
    }
}