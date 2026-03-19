package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class WelcomeViewModel : ViewModel() {

    //Para ayudarnnos a decidi si mostramos o on el card de login.
    var showLoginCard by mutableStateOf(value =false)
        private set

    fun onLoginClicked() {
        showLoginCard = true;
    }
    fun onRegisterClicked() {
        println("TODO: Register desde WelcomeViewModel")
    }

}