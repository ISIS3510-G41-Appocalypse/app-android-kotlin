package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.presentation.viewmodels.RateUserUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.RatingViewModel
import kotlinx.coroutines.delay

@Composable
fun RateUserScreen(
    viewModel: RatingViewModel,
    rideId: Int,
    ratingType: String,
    onBack: () -> Unit,
    onRatingFinished: () -> Unit = {}
) {
    val state = viewModel.uiState
    val normalizedRatingType = remember(ratingType) {
        if (ratingType == "rider") "rider" else "driver"
    }
    val isRiderWithoutSelection = normalizedRatingType == "rider" && state.selectedRiderId == null
    val noPendingRidersMessage = "No hay pasajeros pendientes por calificar."
    val driverAlreadyRatedMessage = "Ya calificaste al conductor de este viaje."
    val offlinePendingSavedMessage = "Sin conexión. Guardamos tu calificación para que puedas enviarla después."
    val isNoPendingRidersState = normalizedRatingType == "rider" && state.errorMessage == noPendingRidersMessage
    val isDriverAlreadyRatedState = normalizedRatingType == "driver" && state.errorMessage == driverAlreadyRatedMessage
    val isFinalSuccess = state.successMessage != null &&
        (normalizedRatingType != "rider" || state.ridersToRate.isEmpty())
    val shouldShowBackToTrips = isFinalSuccess || isNoPendingRidersState || isDriverAlreadyRatedState

    LaunchedEffect(rideId, normalizedRatingType) {
        viewModel.loadRatingData(rideId, normalizedRatingType)
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage == offlinePendingSavedMessage) {
            delay(1800)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (normalizedRatingType == "driver") "Calificar conductor" else "Calificar pasajero",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Califica tu experiencia después de este viaje.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        state.errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFB91C1C),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEE2E2), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.successMessage?.let {
            Text(
                text = it,
                color = Color(0xFF065F46),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD1FAE5), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (normalizedRatingType == "rider" && !shouldShowBackToTrips) {
            RiderSelector(
                riders = state.ridersToRate,
                selectedRiderId = state.selectedRiderId,
                onSelect = viewModel::selectRider
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (normalizedRatingType == "driver" && !shouldShowBackToTrips) {
            DriverInfoCard(
                driverName = state.ridersToRate.firstOrNull()?.name
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!shouldShowBackToTrips) {
            ScoreSelector(
                label = "Puntualidad",
                selected = state.punctuality,
                onValueSelected = viewModel::updatePunctuality
            )

            Spacer(modifier = Modifier.height(10.dp))

            ScoreSelector(
                label = "Comportamiento",
                selected = state.behavior,
                onValueSelected = viewModel::updateBehavior
            )

            Spacer(modifier = Modifier.height(10.dp))

            ScoreSelector(
                label = "Comunicación",
                selected = state.communication,
                onValueSelected = viewModel::updateCommunication
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (normalizedRatingType == "driver") {
                ScoreSelector(
                    label = "Seguridad",
                    selected = state.security,
                    onValueSelected = viewModel::updateSecurity
                )
            } else {
                ScoreSelector(
                    label = "Puntualidad en el pago",
                    selected = state.paymentPunctuality,
                    onValueSelected = viewModel::updatePaymentPunctuality
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        if (shouldShowBackToTrips) {
            Button(
                onClick = {
                    viewModel.clearMessages()
                    onRatingFinished()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Volver a mis viajes")
            }
        } else {
            Button(
                onClick = viewModel::submitRating,
                enabled = !state.isSubmitting && !isRiderWithoutSelection,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (state.isSubmitting) "Enviando..." else "Enviar calificación")
            }
        }

        if (!shouldShowBackToTrips) {
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    viewModel.clearMessages()
                    onBack()
                }
            ) {
                Text("Más tarde")
            }
        }
    }
}

@Composable
private fun DriverInfoCard(driverName: String?) {
    if (driverName.isNullOrBlank()) return

    Text(
        text = "Conductor",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = driverName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun RiderSelector(
    riders: List<RateUserUiModel>,
    selectedRiderId: Int?,
    onSelect: (Int) -> Unit
) {
    if (riders.isEmpty()) return

    Text(
        text = "Selecciona un pasajero",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        riders.forEach { rider ->
            val riderId = rider.riderId ?: return@forEach
            val isSelected = selectedRiderId == riderId
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

            Button(
                onClick = { onSelect(riderId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor)
            ) {
                Text(
                    text = rider.name,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ScoreSelector(
    label: String,
    selected: Int,
    onValueSelected: (Int) -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..5).forEach { value ->
            val isSelected = value == selected
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            Button(
                onClick = { onValueSelected(value) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)
            ) {
                Text(text = value.toString())
            }
        }
    }
}


