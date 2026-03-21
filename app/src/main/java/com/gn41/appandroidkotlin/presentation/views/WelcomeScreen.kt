package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.presentation.components.LoginCard
import com.gn41.appandroidkotlin.ui.theme.BrightSnow
import com.gn41.appandroidkotlin.ui.theme.CoolSteel
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel

@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel){
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.SpaceBetween){


        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally){

            WelcomeHeader()
            if (viewModel.showLoginCard) {
                LoginCard(viewModel = viewModel)
            }
            else {

                WelcomeMessage()
                WelcomeButtons(onLoginClick = {viewModel.onLoginClicked()}, onRegisterClick = {viewModel.onRegisterClicked()})
            }

        }
    }
}


//Logo, titulo
@Composable
fun WelcomeHeader(){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = "Logo HappyRide",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Happy Ride",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Universidad de los Andes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
// slogan simple
@Composable
fun WelcomeMessage(){

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(text = "Muévete fácil\ndentro de", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Uniandes", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(height = 16.dp))

        Text(text = "Comparte wheels de forma simple y sin complicaciones.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Your ride. No stress.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,color = MaterialTheme.colorScheme.onSurface)



    }

}

// esto es el boton de registro y de login
@Composable
fun WelcomeButtons(onLoginClick: () -> Unit, onRegisterClick: () -> Unit){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Button(onClick = onRegisterClick){ Text(text = "Registrarse")}
    Button(onClick = onLoginClick,   colors = ButtonDefaults.buttonColors( containerColor = CoolSteel,contentColor = BrightSnow
    )) {Text(text = "¿Ya tienes una cuenta? Inicia Sesión")}
    }
}