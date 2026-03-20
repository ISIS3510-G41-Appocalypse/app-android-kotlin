package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val darkBlue = Color(0xFF0B1E3B)      // background
val cyanPrimary = Color(0xFF0FA3B1)   // secondary color
val orangePrimary = Color(0xFFE76F00) // reserve button
val whiteCard = Color(0xFFF5F7FA)     // card background

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBlue)
            .padding(16.dp)
    ) {

        // App name → titleLarge (Lexend Bold)
        Text(
            text = "HappyRide",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section header → titleLarge (Lexend Bold)
        Text(
            text = "Oferta de viajes",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )

        // Subtitle description → bodyMedium (Space Grotesk Normal)
        Text(
            text = "Encuentra el viaje perfecto para tu trayecto.",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilterCard()

        Spacer(modifier = Modifier.height(16.dp))

        RideCard(
            name = "Carlos Méndez",
            price = "$4.500"
        )

        Spacer(modifier = Modifier.height(12.dp))

        RideCard(
            name = "Ana María Silva",
            price = "$5.000"
        )
    }
}

@Composable
fun FilterCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        // Filter label → titleMedium (Lexend Bold)
        Text(
            text = "Zona",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            // Option value → bodyMedium (Space Grotesk Normal)
            Text(text = "Colina", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Día", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "Hoy", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Tipo de viaje", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "Llegada a la un", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun RideCard(name: String, price: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Driver name → titleMedium (Lexend Bold)
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium
            )
            // Price → titleMedium (Lexend Bold) with accent color
            Text(
                text = price,
                color = orangePrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Route details → bodyMedium (Space Grotesk Normal)
        Text(text = "CC Parque Colina",           style = MaterialTheme.typography.bodyMedium)
        Text(text = "Universidad de los Andes",   style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Reservar", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}