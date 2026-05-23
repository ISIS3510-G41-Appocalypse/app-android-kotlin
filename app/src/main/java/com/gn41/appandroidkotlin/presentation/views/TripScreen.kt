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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gn41.appandroidkotlin.domain.UserSharedLocation
import com.gn41.appandroidkotlin.presentation.components.TripLocationCard
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveDriverTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRiderTripUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.MapUserMarkerUiState
import com.gn41.appandroidkotlin.presentation.viewmodels.TripReservationItemUiModel
import com.gn41.appandroidkotlin.presentation.viewmodels.TripViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.normalizeState
import com.gn41.appandroidkotlin.presentation.viewmodels.stateToReadableLabel
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TripScreen(
    viewModel: TripViewModel,
    onHomeClick: () -> Unit,
    onPagosClick: () -> Unit,
    onRateRidersClick: (Int) -> Unit,
    onRateDriverClick: (Int) -> Unit
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf("Conductor") }
    var reservationToAcceptId by remember { mutableStateOf<Int?>(null) }
    var reservationToRejectId by remember { mutableStateOf<Int?>(null) }
    var reservationToCancelId by remember { mutableStateOf<Int?>(null) }
    var showCancelRideDialog by remember { mutableStateOf(false) }
    var showFinishRideDialog by remember { mutableStateOf(false) }
    var showRateRidersDialog by remember { mutableStateOf(false) }
    var popupShownForRideId by rememberSaveable { mutableStateOf<Int?>(null) }
    var finishRequestedRideId by rememberSaveable { mutableStateOf<Int?>(null) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val canAutoRefresh = remember(viewModel.connectivity, state.isOfflineData) {
        viewModel.connectivity && !state.isOfflineData
    }
    val hasPendingDriverRating = remember(state.finishedRideIdForRating, state.activeDriverTrip) {
        state.finishedRideIdForRating != null && state.activeDriverTrip == null
    }
    val hasPendingRiderRating = remember(state.finishedRiderRideIdForRating, state.activeRiderTrips) {
        state.finishedRiderRideIdForRating != null && state.activeRiderTrips.isEmpty()
    }

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

    LaunchedEffect(state.finishedRideIdForRating) {
        if (
            state.finishedRideIdForRating != null &&
            finishRequestedRideId == state.finishedRideIdForRating &&
            popupShownForRideId != state.finishedRideIdForRating
        ) {
            showRateRidersDialog = true
            popupShownForRideId = state.finishedRideIdForRating
            finishRequestedRideId = null
        }
    }

    LaunchedEffect(canAutoRefresh) {
        if (!canAutoRefresh) return@LaunchedEffect
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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mis viajes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Revisa tu viaje como conductor o pasajero.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
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

        if (state.isOfflineData && state.offlineMessage.isNotEmpty()) {
            Text(
                text = state.offlineMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF78350F),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (hasPendingDriverRating) {
            PendingRatingCard(
                title = "Calificación pendiente",
                message = "Tu viaje fue finalizado. Puedes calificar a tus pasajeros.",
                buttonText = "Calificar pasajeros",
                onRate = {
                    state.finishedRideIdForRating?.let { onRateRidersClick(it) }
                },
                onSkip = viewModel::clearFinishedRideForRating
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (hasPendingRiderRating) {
            PendingRatingCard(
                title = "Calificación pendiente",
                message = "Tu viaje fue finalizado. Puedes calificar al conductor.",
                buttonText = "Calificar conductor",
                onRate = {
                    state.finishedRiderRideIdForRating?.let { onRateDriverClick(it) }
                },
                onSkip = viewModel::clearFinishedRiderRideForRating
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Cargando mis viajes...",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
                        onAcceptReservation = { reservationToAcceptId = it },
                        onRejectReservation = { reservationToRejectId = it },
                        onCancelTrip = { showCancelRideDialog = true },
                        onStartTrip = viewModel::onStartTripClicked,
                        onOpenRoute = viewModel::onOpenRouteClicked,
                        onFinishTrip = { showFinishRideDialog = true },
                        onCancelReservation = { reservationToCancelId = it },
                        isOfflineMode = state.isOfflineData
                    )
                }

                else -> {
                    PortraitTripsContent(
                        viewModel = viewModel,
                        selectedSection = selectedSection,
                        onSectionSelected = { selectedSection = it },
                        driverTrip = state.activeDriverTrip,
                        riderTrips = state.activeRiderTrips,
                        onAcceptReservation = { reservationToAcceptId = it },
                        onRejectReservation = { reservationToRejectId = it },
                        onCancelTrip = { showCancelRideDialog = true },
                        onStartTrip = viewModel::onStartTripClicked,
                        onOpenRoute = viewModel::onOpenRouteClicked,
                        onFinishTrip = { showFinishRideDialog = true },
                        onCancelReservation = { reservationToCancelId = it },
                        isOfflineMode = state.isOfflineData
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        BottomNavigationBar(
            selectedTab = "Viajes",
            onTabClick = {
                if (it == "Inicio") {
                    onHomeClick()
                }
                else if (it == "Pagos") {
                    onPagosClick()
                }
            }
        )
    }

    if (reservationToAcceptId != null) {
        AlertDialog(
            onDismissRequest = { reservationToAcceptId = null },
            title = { Text("¿Aceptar esta reserva?") },
            text = { Text("El pasajero sera agregado a tu viaje.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reservationId = reservationToAcceptId ?: return@TextButton
                        viewModel.onAcceptReservationClicked(reservationId)
                        reservationToAcceptId = null
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToAcceptId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (reservationToRejectId != null) {
        AlertDialog(
            onDismissRequest = { reservationToRejectId = null },
            title = { Text("¿Rechazar esta reserva?") },
            text = { Text("El pasajero podra buscar otro viaje.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reservationId = reservationToRejectId ?: return@TextButton
                        viewModel.onRejectReservationClicked(reservationId)
                        reservationToRejectId = null
                    }
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToRejectId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showCancelRideDialog) {
        AlertDialog(
            onDismissRequest = { showCancelRideDialog = false },
            title = { Text("Cancelar viaje") },
            text = { Text("¿Deseas cancelar este viaje? Las reservas activas asociadas serán rechazadas.") },
            dismissButton = {
                TextButton(onClick = { showCancelRideDialog = false }) {
                    Text("Volver")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelRideDialog = false
                        viewModel.onCancelTripClicked()
                    }
                ) {
                    Text("Cancelar viaje")
                }
            }
        )
    }

    if (showFinishRideDialog) {
        AlertDialog(
            onDismissRequest = { showFinishRideDialog = false },
            title = { Text("Finalizar viaje") },
            text = { Text("¿Deseas finalizar este viaje?") },
            dismissButton = {
                TextButton(onClick = { showFinishRideDialog = false }) {
                    Text("Volver")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val rideId = state.activeDriverTrip?.rideId ?: return@TextButton
                        showFinishRideDialog = false
                        finishRequestedRideId = rideId
                        viewModel.onFinishTripClicked()
                    }
                ) {
                    Text("Finalizar")
                }
            }
        )
    }

    if (showRateRidersDialog && state.finishedRideIdForRating != null) {
        AlertDialog(
            onDismissRequest = {
                showRateRidersDialog = false
            },
            title = { Text("Viaje finalizado") },
            text = { Text("¿Deseas calificar a tus pasajeros ahora?") },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRateRidersDialog = false
                    }
                ) {
                    Text("Más tarde")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRateRidersDialog = false
                        state.finishedRideIdForRating?.let { onRateRidersClick(it) }
                    }
                ) {
                    Text("Calificar ahora")
                }
            }
        )
    }

    if (reservationToCancelId != null) {
        AlertDialog(
            onDismissRequest = { reservationToCancelId = null },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Deseas cancelar tu reserva para este viaje?") },
            dismissButton = {
                TextButton(onClick = { reservationToCancelId = null }) {
                    Text("Volver")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reservationId = reservationToCancelId ?: return@TextButton
                        reservationToCancelId = null
                        viewModel.onCancelReservationClicked(reservationId)
                    }
                ) {
                    Text("Cancelar reserva")
                }
            }
        )
    }
}

@Composable
private fun PendingRatingCard(
    title: String,
    message: String,
    buttonText: String,
    onRate: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(
                text = buttonText,
                onClick = onRate,
                accentColor = MaterialTheme.colorScheme.secondary
            )
            SmallActionButton(
                text = "Omitir",
                onClick = onSkip,
                accentColor = MaterialTheme.colorScheme.tertiary
            )
        }
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
    onCancelReservation: (Int) -> Unit,
    isOfflineMode: Boolean = false
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
                    onFinishTrip = onFinishTrip,
                    isOfflineMode = isOfflineMode
                )
            } else {
                RiderSection(
                    viewModel = viewModel,
                    trips = riderTrips,
                    onCancelReservation = onCancelReservation,
                    isOfflineMode = isOfflineMode
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
    onCancelReservation: (Int) -> Unit,
    isOfflineMode: Boolean = false
) {
    val state = viewModel.uiState

    val sharedUsersCount = remember(state.rideLocations) {
        state.rideLocations
            .map { it.userId }
            .distinct()
            .size
    }

    val totalUsersInRide = remember(selectedSection, driverTrip?.reservationsCount, sharedUsersCount) {
        if (selectedSection == "Conductor") {
            (driverTrip?.reservationsCount ?: 0) + 1
        } else {
            sharedUsersCount
        }
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
                        onFinishTrip = onFinishTrip,
                        isOfflineMode = isOfflineMode
                    )
                } else {
                    RiderSummarySection(
                        trips = riderTrips,
                        onCancelReservation = onCancelReservation,
                        isOfflineMode = isOfflineMode
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
                    TripLocationSection(
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
                        onRefreshLocations = viewModel::loadLocationsForCurrentRide,
                        mapMarkersProvider = viewModel::getMapMarkers,
                        isOfflineMode = isOfflineMode
                    )
                } else {
                    EmptyStateCardTrip(message = "No hay un viaje activo para mostrar en el mapa.")
                }
            } else {
                val firstTrip = riderTrips.firstOrNull()
                if (firstTrip != null) {
                    TripLocationSection(
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
                        onRefreshLocations = viewModel::loadLocationsForCurrentRide,
                        mapMarkersProvider = viewModel::getMapMarkers,
                        isOfflineMode = isOfflineMode
                    )
                } else {
                    EmptyStateCardTrip(message = "No hay una reserva activa para mostrar en el mapa.")
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
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val selected = item == selectedSection
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary,
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
private fun TripLocationSection(
    isDriver: Boolean,
    isLocationSharingEnabled: Boolean,
    onToggleLocationSharing: (Boolean) -> Unit,
    hasLocationPermission: Boolean,
    currentLatitude: Double?,
    currentLongitude: Double?,
    sharedUsersCount: Int,
    totalUsersInRide: Int,
    rideLocations: List<UserSharedLocation>,
    currentUserId: Int,
    isUsingCachedLocations: Boolean,
    cachedLocationMessage: String,
    onRefreshLocations: () -> Unit,
    mapMarkersProvider: () -> List<MapUserMarkerUiState>,
    isOfflineMode: Boolean = false
) {
    var showMap by rememberSaveable { mutableStateOf(false) }

    if (!showMap) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Ubicación del viaje",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "El mapa se cargará cuando lo necesites.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = { showMap = true }) {
                Text("Ver mapa")
            }
        }
        return
    }

    TripLocationCard(
        isDriver = isDriver,
        isLocationSharingEnabled = isLocationSharingEnabled,
        onToggleLocationSharing = onToggleLocationSharing,
        hasLocationPermission = hasLocationPermission,
        currentLatitude = currentLatitude,
        currentLongitude = currentLongitude,
        sharedUsersCount = sharedUsersCount,
        totalUsersInRide = totalUsersInRide,
        rideLocations = rideLocations,
        currentUserId = currentUserId,
        isUsingCachedLocations = isUsingCachedLocations,
        cachedLocationMessage = cachedLocationMessage,
        onRefreshLocations = onRefreshLocations,
        mapMarkers = mapMarkersProvider(),
        isOfflineMode = isOfflineMode
    )
}

@Composable
private fun RiderSection(
    viewModel: TripViewModel,
    trips: List<ActiveRiderTripUiModel>,
    onCancelReservation: (Int) -> Unit,
    isOfflineMode: Boolean = false
) {
    if (trips.isEmpty()) {
        EmptyStateCardTrip(message = "No tienes una reserva activa como pasajero.")
        return
    }

    val state = viewModel.uiState

    val sharedUsersCount = remember(state.rideLocations) {
        state.rideLocations
            .map { it.userId }
            .distinct()
            .size
    }

    val totalUsersInRide = sharedUsersCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Mis reservas activas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(trips) { trip ->
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RiderReservationCard(
                    trip = trip,
                    onCancel = { onCancelReservation(trip.reservationId) },
                    isOfflineMode = isOfflineMode
                )

                TripLocationSection(
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
                    onRefreshLocations = viewModel::loadLocationsForCurrentRide,
                    mapMarkersProvider = viewModel::getMapMarkers,
                    isOfflineMode = isOfflineMode
                )
            }
        }
    }
}

@Composable
private fun RiderSummarySection(
    trips: List<ActiveRiderTripUiModel>,
    onCancelReservation: (Int) -> Unit,
    isOfflineMode: Boolean = false
) {
    if (trips.isEmpty()) {
        EmptyStateCardTrip(message = "No tienes una reserva activa como pasajero.")
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(trips) { trip ->
            RiderReservationCard(
                trip = trip,
                onCancel = { onCancelReservation(trip.reservationId) },
                isOfflineMode = isOfflineMode
            )
        }
    }
}

@Composable
private fun RiderReservationCard(
    trip: ActiveRiderTripUiModel,
    onCancel: () -> Unit,
    isOfflineMode: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
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

        Text("Estado reserva: ${mapStateLabel(trip.status)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Estado viaje: ${mapStateLabel(trip.rideStatus)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Conductor: ${trip.driverName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Fecha de salida: ${trip.departureDate}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Hora de salida: ${formatTimeText(trip.departureTime)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(10.dp))

        if (trip.showCancelButton) {
            Button(
                onClick = onCancel,
                enabled = trip.canCancelReservation && !isOfflineMode,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text("Cancelar reserva")
            }

            if (trip.cancelDisabledReason != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = trip.cancelDisabledReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            if (isOfflineMode && trip.canCancelReservation) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No disponible en modo offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
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
    onFinishTrip: () -> Unit,
    isOfflineMode: Boolean = false
) {
    if (trip == null) {
        EmptyStateCardTrip(message = "No tienes un viaje activo como conductor.")
        return
    }

    val state = viewModel.uiState

    val sharedUsersCount = remember(state.rideLocations) {
        state.rideLocations
            .map { it.userId }
            .distinct()
            .size
    }

    val totalUsersInRide = trip.reservationsCount + 1

    val canManageReservations = normalizeState(trip.status) == "OFERTADO"
    val visibleReservations = if (normalizeState(trip.status) == "EN_CURSO") {
        trip.reservations.filter {
            val reservationState = normalizeState(it.status)
            reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
        }
    } else {
        trip.reservations
    }

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
                    onFinishTrip = onFinishTrip,
                    isOfflineMode = isOfflineMode
                )

                TripLocationSection(
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
                    onRefreshLocations = viewModel::loadLocationsForCurrentRide,
                    mapMarkersProvider = viewModel::getMapMarkers,
                    isOfflineMode = isOfflineMode
                )
            }
        }

        if (visibleReservations.isNotEmpty()) {
            item {
                Text(
                    text = "Reservas actuales",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(visibleReservations) { reservation ->
                DriverReservationRow(
                    item = reservation,
                    canAccept = trip.availableSeats > 0,
                    canManageReservation = canManageReservations,
                    onAccept = { onAcceptReservation(reservation.id) },
                    onReject = { onRejectReservation(reservation.id) },
                    isOfflineMode = isOfflineMode
                )
            }
        } else {
            item {
                EmptyStateCardTrip(message = "No tienes ofertas sobre este viaje.")
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
    onFinishTrip: () -> Unit,
    isOfflineMode: Boolean = false
) {
    if (trip == null) {
        EmptyStateCardTrip(message = "No tienes un viaje activo como conductor.")
        return
    }

    val canManageReservations = normalizeState(trip.status) == "OFERTADO"
    val visibleReservations = if (normalizeState(trip.status) == "EN_CURSO") {
        trip.reservations.filter {
            val reservationState = normalizeState(it.status)
            reservationState == "ACEPTADA" || reservationState == "EN_CURSO"
        }
    } else {
        trip.reservations
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
                onFinishTrip = onFinishTrip,
                isOfflineMode = isOfflineMode
            )
        }

        if (visibleReservations.isNotEmpty()) {
            item {
                Text(
                    text = "Reservas actuales",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(visibleReservations) { reservation ->
                DriverReservationRow(
                    item = reservation,
                    canAccept = trip.availableSeats > 0,
                    canManageReservation = canManageReservations,
                    onAccept = { onAcceptReservation(reservation.id) },
                    onReject = { onRejectReservation(reservation.id) },
                    isOfflineMode = isOfflineMode
                )
            }
        } else {
            item {
                EmptyStateCardTrip(message = "No tienes ofertas sobre este viaje.")
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
    onFinishTrip: () -> Unit,
    isOfflineMode: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("Mi viaje como conductor", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))

        Text("Origen: ${trip.source}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Destino: ${trip.destination}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Estado: ${mapStateLabel(trip.status)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Fecha de salida: ${trip.departureDate}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Hora de salida: ${formatTimeText(trip.departureTime)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Reservas: ${trip.reservationsCount}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Cupos disponibles: ${trip.availableSeats}/${trip.totalSeats}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(10.dp))

        Spacer(modifier = Modifier.height(8.dp))

        if (normalizeState(trip.status) == "OFERTADO") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(
                    text = "Cancelar viaje",
                    onClick = onCancelTrip,
                    enabled = !isOfflineMode,
                    accentColor = MaterialTheme.colorScheme.error
                )
                SmallActionButton(
                    text = "Iniciar",
                    onClick = onStartTrip,
                    enabled = !isOfflineMode,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (normalizeState(trip.status) == "EN_CURSO") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(
                    text = "Abrir ruta",
                    onClick = onOpenRoute,
                    enabled = !isOfflineMode,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
                SmallActionButton(
                    text = "Finalizar",
                    onClick = onFinishTrip,
                    enabled = !isOfflineMode,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (isOfflineMode) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No se pueden realizar acciones en modo offline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

@Composable
private fun DriverReservationRow(
    item: TripReservationItemUiModel,
    canAccept: Boolean,
    canManageReservation: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    isOfflineMode: Boolean = false
) {
    val riderRatingText = item.riderRating
        ?.coerceIn(0.0, 5.0)
        ?.let { rating ->
            "Rating: ${String.format(Locale.getDefault(), "%.1f", rating)} ⭐"
        }
        ?: "Rating: No rating yet"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = item.riderName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Estado: ${mapStateLabel(item.status)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        var color: Color = MaterialTheme.colorScheme.error
        item.cancellationOdds?.let {
            if (it < 0.30) {
                color = MaterialTheme.colorScheme.secondary
            }
        }
        Text(text = riderRatingText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Probabilidad de cancelación: ${item.cancellationOdds?.times(100)}%", color = color)
        Text(text = "Metodo de pago: ${item.paymentMethod}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

        if (normalizeState(item.status) == "PENDIENTE" && canManageReservation) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(
                    text = "Aceptar",
                    onClick = onAccept,
                    enabled = canAccept && !isOfflineMode,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
                SmallActionButton(
                    text = "Rechazar",
                    onClick = onReject,
                    enabled = !isOfflineMode,
                    accentColor = MaterialTheme.colorScheme.error
                )
            }
            if (!canAccept) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No hay cupos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (isOfflineMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No disponible en modo offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        } else if (normalizeState(item.status) == "PENDIENTE" && !canManageReservation) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No puedes gestionar reservas con el viaje en curso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val backgroundColor = if (enabled) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(1.dp, if (enabled) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun EmptyStateCardTrip(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary
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
    return stateToReadableLabel(state)
}

@Composable
private fun StateChip(status: String) {
    val (bg, fg) = when (normalizeState(status)) {
        "PENDIENTE" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "ACEPTADA" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "EN_CURSO" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
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
