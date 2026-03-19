package com.gn41.appandroidkotlin.presentation.viewmodels

import androidx.lifecycle.ViewModel

class WelcomeViewModel : ViewModel() {

    fun onLoginClicked() {
        println("Login desde ViewModel")
    }
    fun onRegisterClicked() {
        println("Register desde ViewModel")
    }

}