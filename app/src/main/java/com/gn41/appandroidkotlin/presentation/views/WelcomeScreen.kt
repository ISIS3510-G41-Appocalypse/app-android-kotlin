package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun WelcomeScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit){
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween){

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally){

            WelcomeHeader()
            WelcomeMessage()

        }


        WelcomeButtons(onLoginClick = onLoginClick, onRegisterClick = onRegisterClick)


    }
}


@Composable
fun WelcomeHeader(){
    Text(text = "Happy Ride", color = MaterialTheme.colorScheme.primary)

}

@Composable
fun WelcomeMessage(){}


@Composable
fun WelcomeButtons(onLoginClick: () -> Unit, onRegisterClick: () -> Unit){
    Button(onClick = onRegisterClick){ Text(text = "Registrarse")}
    Button(onClick = onLoginClick) {Text(text = "¿Ya tienes una cuenta? Inicia Sesión")}
}