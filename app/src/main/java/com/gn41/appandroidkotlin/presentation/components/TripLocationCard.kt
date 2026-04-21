package com.gn41.appandroidkotlin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TripLocationCard(
    isDriver: Boolean,
    isLocationSharingEnabled: Boolean,
    onToggleLocationSharing: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            //   Título
            Text(text = "Ubicación del viaje")

            Spacer(modifier = Modifier.height(12.dp))

            //   Placeholder del mapa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF94A3B8), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Mapa (próximamente)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            //   Texto según rol
            val roleMessage = if (isDriver) {
                "Aquí podrás ver a tus riders cuando compartan ubicación."
            } else {
                "Aquí podrás ver al conductor y tu posición relativa."
            }

            Text(text = roleMessage)

            Spacer(modifier = Modifier.height(12.dp))

            //   Switch ubicación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Compartir mi ubicación")

                Switch(
                    checked = isLocationSharingEnabled,
                    onCheckedChange = onToggleLocationSharing
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            //   Estado
            val statusText = if (isLocationSharingEnabled) {
                "Ubicación compartida"
            } else {
                "Ubicación oculta"
            }

            Text(text = statusText)
        }
    }
}