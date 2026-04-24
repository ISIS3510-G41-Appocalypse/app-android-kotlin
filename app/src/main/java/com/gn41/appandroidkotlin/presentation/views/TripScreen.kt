package com.gn41.appandroidkotlin.presentation.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gn41.appandroidkotlin.presentation.components.TripLocationCard
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveDriverTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRiderTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.TripReservationItemUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModel
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber
import kotlinx.coroutines.delay

@Composable
fun TripScreen(
    viewModel: TripViewModel,
    onHomeClick: () -> Unit
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf("Conductor") }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onLocationPermissionResult(granted)
        if (granted && viewModel.uiState.isLocationSharingEnabled) {
            viewModel.onLocationRequestStarted()
            requestLastKnownLocation(context, viewModel)
        }
    }

    if (state.infoMessage.isNotEmpty()) {
        LaunchedEffect(state.infoMessage) {
            delay(3000)
            viewModel.clearInfoMessage()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            viewModel.refreshTrips()
        }
    }

    LaunchedEffect(state.isLocationSharingEnabled) {
        if (state.isLocationSharingEnabled) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            viewModel.onLocationPermissionResult(granted)

            if (granted) {
                viewModel.onLocationRequestStarted()
                requestLastKnownLocation(context, viewModel)
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBlue)
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(darkBlue)
            .padding(16.dp)
    ) {
        Text(
            text = "Mis viajes",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Revisa tu viaje como conductor o pasajero.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.infoMessage.isNotEmpty()) {
            Text(
                text = state.infoMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF0D9488),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE6FFFA), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                state.errorMessage.isNotEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = Color(0xFFFCA5A5),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                isLandscape -> {
                    LandscapeTripsContent(
                        viewModel = viewModel,
                        selectedSection = selectedSection,
                        onSectionSelected = { selectedSection = it },
                        driverTrip = state.activeDriverTrip,
                        riderTrips = state.activeRiderTrips,
                        onAcceptReservation = viewModel::onAcceptReservationClicked,
                        onRejectReservation = viewModel::onRejectReservationClicked,
                        onCancelTrip = viewModel::onCancelTripClicked,
                        onStartTrip = viewModel::onStartTripClicked,
                        onOpenRoute = viewModel::onOpenRouteClicked,
                        onFinishTrip = viewModel::onFinishTripClicked,
                        onCancelReservation = viewModel::onCancelReservationClicked
                    )
                }

                else -> {
                    PortraitTripsContent(
                        viewModel = viewModel,
                        selectedSection = selectedSection,
                        onSectionSelected = { selectedSection = it },
                        driverTrip = state.activeDriverTrip,
                        riderTrips = state.activeRiderTrips,
                        onAcceptReservation = viewModel::onAcceptReservationClicked,
                        onRejectReservation = viewModel::onRejectReservationClicked,
                        onCancelTrip = viewModel::onCancelTripClicked,
                        onStartTrip = viewModel::onStartTripClicked,
                        onOpenRoute = viewModel::onOpenRouteClicked,
                        onFinishTrip = viewModel::onFinishTripClicked,
                        onCancelReservation = viewModel::onCancelReservationClicked
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        BottomNavigationBar(
            selectedTab = "Viajes",
            onTabClick = {
                if (it == "Inicio") onHomeClick()
            }
        )
    }
}

@Composable
private fun PortraitTripsContent(
    viewModel: TripViewModel,
    selectedSection: String,
    onSectionSelected: (String) -> Unit,
    driverTrip: ActiveDriverTripUiModel?,
    riderTrips: List<ActiveRiderTripUiModel>,
    onAcceptReservation: (Int) -> Unit,
    onRejectReservation: (Int) -> Unit,
    onCancelTrip: () -> Unit,
    onStartTrip: () -> Unit,
    onOpenRoute: () -> Unit,
    onFinishTrip: () -> Unit,
    onCancelReservation: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SectionSwitch(
            selectedSection = selectedSection,
            onSectionSelected = onSectionSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedSection == "Conductor") {
                DriverSection(
                    viewModel = viewModel,
                    trip = driverTrip,
                    onAcceptReservation = onAcceptReservation,
                    onRejectReservation = onRejectReservation,
                    onCancelTrip = onCancelTrip,
                    onStartTrip = onStartTrip,
                    onOpenRoute = onOpenRoute,
                    onFinishTrip = onFinishTrip
                )
            } else {
                RiderSection(
                    viewModel = viewModel,
                    trips = riderTrips,
                    onCancelReservation = onCancelReservation
                )
            }
        }
    }
}

@Composable
private fun LandscapeTripsContent(
    viewModel: TripViewModel,
    selectedSection: String,
    onSectionSelected: (String) -> Unit,
    driverTrip: ActiveDriverTripUiModel?,
    riderTrips: List<ActiveRiderTripUiModel>,
    onAcceptReservation: (Int) -> Unit,
    onRejectReservation: (Int) -> Unit,
    onCancelTrip: () -> Unit,
    onStartTrip: () -> Unit,
    onOpenRoute: () -> Unit,
    onFinishTrip: () -> Unit,
    onCancelReservation: (Int) -> Unit
) {
    val state = viewModel.uiState

    val sharedUsersCount = state.rideLocations
        .map { it.userId }
        .distinct()
        .size

    val totalUsersInRide = if (selectedSection == "Conductor") {
        (driverTrip?.reservationsCount ?: 0) + 1
    } else {
        sharedUsersCount
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
        ) {
            SectionSwitch(
                selectedSection = selectedSection,
                onSectionSelected = onSectionSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (selectedSection == "Conductor") {
                    DriverSummarySection(
                        trip = driverTrip,
                        onAcceptReservation = onAcceptReservation,
                        onRejectReservation = onRejectReservation,
                        onCancelTrip = onCancelTrip,
                        onStartTrip = onStartTrip,
                        onOpenRoute = onOpenRoute,
                        onFinishTrip = onFinishTrip
                    )
                } else {
                    RiderSummarySection(
                        trips = riderTrips,
                        onCancelReservation = onCancelReservation
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
        ) {
            if (selectedSection == "Conductor") {
                if (driverTrip != null) {
                    TripLocationCard(
                        isDriver = true,
                        isLocationSharingEnabled = state.isLocationSharingEnabled,
                        onToggleLocationSharing = viewModel::onToggleLocationSharing,
                        hasLocationPermission = state.hasLocationPermission,
                        currentLatitude = state.currentLatitude,
                        currentLongitude = state.currentLongitude,
                        sharedUsersCount = sharedUsersCount,
                        totalUsersInRide = totalUsersInRide,
                        rideLocations = state.rideLocations,
                        currentUserId = state.currentUserId ?: -1,
                        isUsingCachedLocations = state.isUsingCachedLocations,
                        cachedLocationMessage = state.cachedLocationMessage,
                        onRefreshLocations = viewModel::loadLocationsForCurrentRide
                    )
                } else {
                    EmptyStateCard(message = "No hay un viaje activo para mostrar en el mapa.")
                }
            } else {
                val firstTrip = riderTrips.firstOrNull()
                if (firstTrip != null) {
                    TripLocationCard(
                        isDriver = true,
                        isLocationSharingEnabled = state.isLocationSharingEnabled,
                        onToggleLocationSharing = viewModel::onToggleLocationSharing,
                        hasLocationPermission = state.hasLocationPermission,
                        currentLatitude = state.currentLatitude,
                        currentLongitude = state.currentLongitude,
                        sharedUsersCount = sharedUsersCount,
                        totalUsersInRide = totalUsersInRide,
                        rideLocations = state.rideLocations,
                        currentUserId = state.currentUserId ?: -1,
                        isUsingCachedLocations = state.isUsingCachedLocations,
                        cachedLocationMessage = state.cachedLocationMessage,
                        onRefreshLocations = viewModel::loadLocationsForCurrentRide
                    )
                } else {
                    EmptyStateCard(message = "No hay una reserva activa para mostrar en el mapa.")
                }
            }
        }
    }
}

@Composable
private fun SectionSwitch(
    selectedSection: String,
    onSectionSelected: (String) -> Unit
) {
    val items = listOf("Conductor", "Pasajero")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, RoundedCornerShape(12.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val selected = item == selectedSection
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) Color.White else Color.Gray,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) AutumnEmber else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSectionSelected(item) }
                    .padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun RiderSection(
    viewModel: TripViewModel,
    trips: List<ActiveRiderTripUiModel>,
    onCancelReservation: (Int) -> Unit
) {
    if (trips.isEmpty()) {
        EmptyStateCard(message = "No tienes una reserva activa como pasajero.")
        return
    }

    val state = viewModel.uiState

    val sharedUsersCount = state.rideLocations
        .map { it.userId }
        .distinct()
        .size

    val totalUsersInRide = sharedUsersCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Mis reservas activas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(trips) { trip ->
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RiderReservationCard(
                    trip = trip,
                    onCancel = { onCancelReservation(trip.reservationId) }
                )

                TripLocationCard(
                    isDriver = true,
                    isLocationSharingEnabled = state.isLocationSharingEnabled,
                    onToggleLocationSharing = viewModel::onToggleLocationSharing,
                    hasLocationPermission = state.hasLocationPermission,
                    currentLatitude = state.currentLatitude,
                    currentLongitude = state.currentLongitude,
                    sharedUsersCount = sharedUsersCount,
                    totalUsersInRide = totalUsersInRide,
                    rideLocations = state.rideLocations,
                    currentUserId = state.currentUserId ?: -1,
                    isUsingCachedLocations = state.isUsingCachedLocations,
                    cachedLocationMessage = state.cachedLocationMessage,
                    onRefreshLocations = viewModel::loadLocationsForCurrentRide
                )
            }
        }
    }
}

@Composable
private fun RiderSummarySection(
    trips: List<ActiveRiderTripUiModel>,
    onCancelReservation: (Int) -> Unit
) {
    if (trips.isEmpty()) {
        EmptyStateCard(message = "No tienes una reserva activa como pasajero.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Mis reservas activas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(trips) { trip ->
            RiderReservationCard(
                trip = trip,
                onCancel = { onCancelReservation(trip.reservationId) }
            )
        }
    }
}

@Composable
private fun RiderReservationCard(
    trip: ActiveRiderTripUiModel,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "${trip.source} → ${trip.destination}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            StateChip(status = trip.status)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text("Estado reserva: ${mapStateLabel(trip.status)}", style = MaterialTheme.typography.bodyMedium)
        Text("Estado viaje: ${mapStateLabel(trip.rideStatus)}", style = MaterialTheme.typography.bodyMedium)
        Text("Hora de salida: ${formatTimeText(trip.departureTime)}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = AutumnEmber)
        ) {
            Text("Cancelar reserva")
        }
    }
}

@Composable
private fun DriverSection(
    viewModel: TripViewModel,
    trip: ActiveDriverTripUiModel?,
    onAcceptReservation: (Int) -> Unit,
    onRejectReservation: (Int) -> Unit,
    onCancelTrip: () -> Unit,
    onStartTrip: () -> Unit,
    onOpenRoute: () -> Unit,
    onFinishTrip: () -> Unit
) {
    if (trip == null) {
        EmptyStateCard(message = "No tienes un viaje activo como conductor.")
        return
    }

    val state = viewModel.uiState

    val sharedUsersCount = state.rideLocations
        .map { it.userId }
        .distinct()
        .size

    val totalUsersInRide = trip.reservationsCount + 1

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DriverMainCard(
                    trip = trip,
                    onCancelTrip = onCancelTrip,
                    onStartTrip = onStartTrip,
                    onOpenRoute = onOpenRoute,
                    onFinishTrip = onFinishTrip
                )

                TripLocationCard(
                    isDriver = true,
                    isLocationSharingEnabled = state.isLocationSharingEnabled,
                    onToggleLocationSharing = viewModel::onToggleLocationSharing,
                    hasLocationPermission = state.hasLocationPermission,
                    currentLatitude = state.currentLatitude,
                    currentLongitude = state.currentLongitude,
                    sharedUsersCount = sharedUsersCount,
                    totalUsersInRide = totalUsersInRide,
                    rideLocations = state.rideLocations,
                    currentUserId = state.currentUserId ?: -1,
                    isUsingCachedLocations = state.isUsingCachedLocations,
                    cachedLocationMessage = state.cachedLocationMessage,
                    onRefreshLocations = viewModel::loadLocationsForCurrentRide
                )
            }
        }

        if (trip.reservations.isNotEmpty()) {
            item {
                Text(
                    text = "Reservas actuales",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            items(trip.reservations) { reservation ->
                DriverReservationRow(
                    item = reservation,
                    canAccept = trip.availableSeats > 0,
                    onAccept = { onAcceptReservation(reservation.id) },
                    onReject = { onRejectReservation(reservation.id) }
                )
            }
        } else {
            item {
                EmptyStateCard(message = "No tienes ofertas sobre este viaje.")
            }
        }
    }
}

@Composable
private fun DriverSummarySection(
    trip: ActiveDriverTripUiModel?,
    onAcceptReservation: (Int) -> Unit,
    onRejectReservation: (Int) -> Unit,
    onCancelTrip: () -> Unit,
    onStartTrip: () -> Unit,
    onOpenRoute: () -> Unit,
    onFinishTrip: () -> Unit
) {
    if (trip == null) {
        EmptyStateCard(message = "No tienes un viaje activo como conductor.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            DriverMainCard(
                trip = trip,
                onCancelTrip = onCancelTrip,
                onStartTrip = onStartTrip,
                onOpenRoute = onOpenRoute,
                onFinishTrip = onFinishTrip
            )
        }

        if (trip.reservations.isNotEmpty()) {
            item {
                Text(
                    text = "Reservas actuales",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            items(trip.reservations) { reservation ->
                DriverReservationRow(
                    item = reservation,
                    canAccept = trip.availableSeats > 0,
                    onAccept = { onAcceptReservation(reservation.id) },
                    onReject = { onRejectReservation(reservation.id) }
                )
            }
        } else {
            item {
                EmptyStateCard(message = "No tienes ofertas sobre este viaje.")
            }
        }
    }
}

@Composable
private fun DriverMainCard(
    trip: ActiveDriverTripUiModel,
    onCancelTrip: () -> Unit,
    onStartTrip: () -> Unit,
    onOpenRoute: () -> Unit,
    onFinishTrip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("Mi viaje como conductor", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Text("Origen: ${trip.source}", style = MaterialTheme.typography.bodyMedium)
        Text("Destino: ${trip.destination}", style = MaterialTheme.typography.bodyMedium)
        Text("Estado: ${mapStateLabel(trip.status)}", style = MaterialTheme.typography.bodyMedium)
        Text("Hora de salida: ${formatTimeText(trip.departureTime)}", style = MaterialTheme.typography.bodyMedium)
        Text("Reservas: ${trip.reservationsCount}", style = MaterialTheme.typography.bodyMedium)
        Text("Cupos disponibles: ${trip.availableSeats}/${trip.totalSeats}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(
                text = "Mirar en Google",
                onClick = onOpenRoute,
                accentColor = Color(0xFF2563EB)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (trip.status == "OFERTADO") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(
                    text = "Cancelar viaje",
                    onClick = onCancelTrip,
                    accentColor = Color(0xFFDC2626)
                )
                SmallActionButton(
                    text = "Iniciar",
                    onClick = onStartTrip,
                    accentColor = Color(0xFF16A34A)
                )
            }
        }

        if (trip.status == "EN_CURSO") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(
                    text = "Abrir ruta",
                    onClick = onOpenRoute,
                    accentColor = Color(0xFF2563EB)
                )
                SmallActionButton(
                    text = "Finalizar",
                    onClick = onFinishTrip,
                    accentColor = Color(0xFF16A34A)
                )
            }
        }
    }
}

@Composable
private fun DriverReservationRow(
    item: TripReservationItemUiModel,
    canAccept: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = item.riderName, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Estado: ${mapStateLabel(item.status)}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Metodo de pago: ${item.paymentMethod}", style = MaterialTheme.typography.bodyMedium)

        if (item.status == "PENDIENTE") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(
                    text = "Aceptar",
                    onClick = onAccept,
                    enabled = canAccept,
                    accentColor = Color(0xFF16A34A)
                )
                SmallActionButton(
                    text = "Rechazar",
                    onClick = onReject,
                    accentColor = Color(0xFFDC2626)
                )
            }
            if (!canAccept) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No hay cupos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB45309)
                )
            }
        }
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    accentColor: Color = AutumnEmber
) {
    val backgroundColor = if (enabled) accentColor.copy(alpha = 0.12f) else Color(0xFFF1F5F9)

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(1.dp, if (enabled) accentColor else Color(0xFF94A3B8), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) accentColor else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@SuppressLint("MissingPermission")
private fun requestLastKnownLocation(
    context: Context,
    viewModel: TripViewModel
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return

    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER
    )

    var bestLocation: Location? = null

    providers.forEach { provider ->
        val location = locationManager.getLastKnownLocation(provider)
        if (location != null) {
            if (bestLocation == null || location.time > bestLocation!!.time) {
                bestLocation = location
            }
        }
    }

    if (bestLocation != null) {
        viewModel.onLocationUpdated(
            latitude = bestLocation!!.latitude,
            longitude = bestLocation!!.longitude
        )
    } else {
        viewModel.onLocationRequestFailed("No se pudo obtener la ubicación.")
    }
}

private fun formatTimeText(rawTime: String): String {
    val parts = rawTime.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else rawTime
}

private fun mapStateLabel(state: String): String {
    return when (state) {
        "OFERTADO" -> "Ofertado"
        "PENDIENTE" -> "Pendiente"
        "ACEPTADA" -> "Aceptada"
        "EN_CURSO" -> "En curso"
        "FINALIZADO" -> "Finalizado"
        "FINALIZADA" -> "Finalizada"
        "CANCELADO" -> "Cancelado"
        "CANCELADA" -> "Cancelada"
        "RECHAZADA" -> "Rechazada"
        else -> state
    }
}

@Composable
private fun StateChip(status: String) {
    val (bg, fg) = when (status) {
        "PENDIENTE" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "ACEPTADA" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "EN_CURSO" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = mapStateLabel(status),
            style = MaterialTheme.typography.bodyMedium,
            color = fg
        )
    }
}