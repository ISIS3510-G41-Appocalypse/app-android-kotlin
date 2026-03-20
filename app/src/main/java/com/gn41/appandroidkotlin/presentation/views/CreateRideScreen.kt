package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel

@Composable
fun CreateRideScreen(viewModel: CreateRideViewModel) {
    val formState = viewModel.formState
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(text = "Crear Ride", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = formState.vehicleId,
            onValueChange = { viewModel.onVehicleSelected(it) },
            label = { Text("Vehículo") },
            modifier = Modifier.fillMaxWidth()
        )

        // 📍 Zona
        OutlinedTextField(
            value = formState.zoneId,
            onValueChange = { viewModel.onZoneSelected(it) },
            label = { Text("Zona") },
            modifier = Modifier.fillMaxWidth()
        )

        // 🟢 Origen
        OutlinedTextField(
            value = formState.startLocation,
            onValueChange = viewModel::onStartLocationChanged,
            label = { Text("Punto de salida") },
            modifier = Modifier.fillMaxWidth()
        )

        // 🔴 Destino
        OutlinedTextField(
            value = formState.endLocation,
            onValueChange = viewModel::onEndLocationChanged,
            label = { Text("Destino") },
            modifier = Modifier.fillMaxWidth()
        )

        // 💰 Precio
        OutlinedTextField(
            value = formState.pricePerSeat,
            onValueChange = viewModel::onPriceChanged,
            label = { Text("Precio por asiento") },
            modifier = Modifier.fillMaxWidth()
        )

        // 📅 Fecha/Hora
        OutlinedTextField(
            value = formState.departureDateTime,
            onValueChange = viewModel::onDateTimeSelected,
            label = { Text("Fecha y hora") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🚀 Botón crear
        Button(
            onClick = { viewModel.createRide() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Ride")
        }

        // 🔄 Loading
        if (uiState is CreateRideUiState.Loading) {
            CircularProgressIndicator()
        }

        // ❌ Error
        if (uiState is CreateRideUiState.Error) {
            Text(
                text = uiState.message,
                color = Color.Red
            )
        }

        // ✅ Success
        if (uiState is CreateRideUiState.Success) {
            Text(
                text = "Ride creado exitosamente 🎉",
                color = Color.Green
            )
        }
    }
}