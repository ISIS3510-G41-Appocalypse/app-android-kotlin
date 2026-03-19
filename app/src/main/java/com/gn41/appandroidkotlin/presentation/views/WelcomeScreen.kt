package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text

@Composable
fun WelcomeScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit){
    Column(){

        WelcomeHeader()
        WelcomeMessage()
        WelcomeButtons(onLoginClick = onLoginClick, onRegisterClick = onRegisterClick)


    }
}


@Composable
fun WelcomeHeader(){
    Text(text = "Happy Ride")

}

@Composable
fun WelcomeMessage(){}


@Composable
fun WelcomeButtons(onLoginClick: () -> Unit, onRegisterClick: () -> Unit){
    Button(onClick = onRegisterClick){ Text(text = "Registrarse")}
    Button(onClick = onLoginClick) {Text(text = "¿Ya tienes una cuenta? Inicia Sesión")}
}