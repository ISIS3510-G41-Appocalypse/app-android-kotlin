package com.gn41.appandroidkotlin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.ui.theme.BrightSnow
import com.gn41.appandroidkotlin.ui.theme.CoolSteel
// imports para poder escribir teclado y contraseñas

import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.gn41.appandroidkotlin.presentation.viewmodels.WelcomeViewModel
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber


@Composable
fun LoginCard(viewModel: WelcomeViewModel)
{
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = BrightSnow, shape = RoundedCornerShape(24.dp)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text( text = "Iniciar sesión", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.background)
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Bienvenido de nuevo", style = MaterialTheme.typography.bodyLarge, color = CoolSteel)
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Correo electrónico", color = MaterialTheme.colorScheme.background)
            Spacer(modifier = Modifier.height(8.dp))

           //aqui va el textfield del correo
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = {viewModel.onEmailInput(it)},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ejemplo@uniandes.edu.co") },
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Contraseña", color = MaterialTheme.colorScheme.background)
                //En el futuro esto puede ser un botón... por ahora lo dejaremos como text
                Text(text = "¿Olvidaste tu contraseña?", color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            //aqui va el textfield de la contraseña
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordInput(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••", color = CoolSteel) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp)
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onLoginSubmit() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(text = "Iniciar sesión")
        }

        // Error message aquí
        if (viewModel.loginError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = viewModel.loginError,
                color = AutumnEmber,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            Text(text = "¿No tienes cuenta? ", color = MaterialTheme.colorScheme.background)
            Text(text = "Regístrate", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
    }
}




