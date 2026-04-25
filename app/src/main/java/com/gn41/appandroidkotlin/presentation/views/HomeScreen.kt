package com.gn41.appandroidkotlin.presentation.views

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gn41.appandroidkotlin.presentation.components.RideItemCard
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModel
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber
import com.gn41.appandroidkotlin.ui.theme.BrightSnow
import com.gn41.appandroidkotlin.ui.theme.CoolSteel
import com.gn41.appandroidkotlin.ui.theme.DarkCyan
import com.gn41.appandroidkotlin.ui.theme.PrussianBlue
import kotlinx.coroutines.delay

val darkBlue = Color(0xFF0B1E3B)
val headerBlue = Color(0xFF1A2744)
val whiteCard = BrightSnow
val selectedBottomItemColor = AutumnEmber

// ─────────────────────────────────────────────────────────────────────────────
// Estados visuales para la zona de rides
// ─────────────────────────────────────────────────────────────────────────────

/** FASE 3 — Vista offline: reemplaza solo la zona de rides */
@Composable
private fun OfflineStateView() {
    EmptyStateCard(
        icon = Icons.Default.WifiOff,
        iconTint = AutumnEmber,
        title = "Sin conexión a internet",
        message = "No podemos cargar los viajes ahora mismo.\nRevisa tu conexión e intenta de nuevo."
    )
}

/** FASE 6 caso A — Hay internet pero no hay rides ofertados en absoluto */
@Composable
private fun EmptyRidesStateView() {
    EmptyStateCard(
        icon = Icons.Default.DirectionsCar,
        iconTint = CoolSteel,
        title = "Sin viajes disponibles",
        message = "No hay viajes disponibles en este momento."
    )
}

/** FASE 6 caso B — Hay internet, hay rides, pero los filtros no devuelven resultados */
@Composable
private fun EmptyFilteredStateView() {
    EmptyStateCard(
        icon = Icons.Default.SearchOff,
        iconTint = CoolSteel,
        title = "Sin resultados",
        message = "Intenta cambiar los filtros para encontrar más viajes."
    )
}

/** Card reutilizable para cualquier estado vacío */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(headerBlue, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                color = CoolSteel,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen principal
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTripsClick: () -> Unit,
    onCreateRideClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val state = viewModel.uiState
    var selectedBottomTab by remember { mutableStateOf("Inicio") }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.refreshNetworkState()
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNetworkState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrussianBlue)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (isLandscape) {
                HomeHeader(onLogoutClick = { viewModel.logout { onLogoutClick() } })

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Columna izquierda: titulo + filtros
                    LazyColumn(
                        modifier = Modifier.weight(0.4f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { OfertaViajestitle() }
                        item {
                            FilterCard(
                                selectedZone = state.selectedZone,
                                zoneOptions = state.zoneOptions,
                                selectedDay = state.selectedDay,
                                selectedTripType = state.selectedTripType,
                                selectedDepartureTime = state.selectedDepartureTime,
                                departureOptions = state.departureTimeOptions,
                                hasActiveFilters = state.hasActiveFilters,
                                activeFilterCount = state.activeFilterCount,
                                onZoneChange = viewModel::onZoneChange,
                                onDayChange = viewModel::onDayChange,
                                onTripTypeChange = viewModel::onTripTypeChange,
                                onDepartureTimeChange = viewModel::onDepartureTimeChange,
                                onClearFilters = viewModel::clearFilters
                            )
                        }
                    }

                    // Columna derecha: rides / estados
                    LazyColumn(
                        modifier = Modifier.weight(0.6f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mensaje de reserva con auto-dismiss
                        if (state.reservationMessage.isNotEmpty()) {
                            item {
                                ReservationMessageBanner(
                                    message = state.reservationMessage,
                                    onDismiss = viewModel::clearReservationMessage
                                )
                            }
                        }

                        when {
                            state.isOffline -> item { OfflineStateView() }

                            state.isLoading -> item { LoadingView() }

                            state.errorMessage.isNotEmpty() -> item {
                                ErrorView(message = state.errorMessage)
                            }

                            state.rides.isNotEmpty() -> {
                                items(items = state.rides, key = { it.id }) { ride ->
                                    RideItemCard(
                                        ride = ride,
                                        onReserveClick = { viewModel.onReserveClicked(ride.id) },
                                        isReserveEnabled = !state.hasActiveRiderReservation &&
                                            !state.hasActiveDriverTrip &&
                                            ride.availableSlots > 0
                                    )
                                }
                            }

                            state.hasActiveFilters -> item { EmptyFilteredStateView() }

                            else -> item { EmptyRidesStateView() }
                        }
                    }
                }

            } else {
                // Portrait
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { HomeHeader(onLogoutClick = { viewModel.logout { onLogoutClick() } }) }
                    item { OfertaViajestitle() }
                    item {
                        FilterCard(
                            selectedZone = state.selectedZone,
                            zoneOptions = state.zoneOptions,
                            selectedDay = state.selectedDay,
                            selectedTripType = state.selectedTripType,
                            selectedDepartureTime = state.selectedDepartureTime,
                            departureOptions = state.departureTimeOptions,
                            hasActiveFilters = state.hasActiveFilters,
                            activeFilterCount = state.activeFilterCount,
                            onZoneChange = viewModel::onZoneChange,
                            onDayChange = viewModel::onDayChange,
                            onTripTypeChange = viewModel::onTripTypeChange,
                            onDepartureTimeChange = viewModel::onDepartureTimeChange,
                            onClearFilters = viewModel::clearFilters
                        )
                    }

                    if (state.reservationMessage.isNotEmpty()) {
                        item {
                            ReservationMessageBanner(
                                message = state.reservationMessage,
                                onDismiss = viewModel::clearReservationMessage
                            )
                        }
                    }

                    when {
                        state.isOffline -> item { OfflineStateView() }

                        state.isLoading -> item { LoadingView() }

                        state.errorMessage.isNotEmpty() -> item {
                            ErrorView(message = state.errorMessage)
                        }

                        state.rides.isNotEmpty() -> {
                            items(items = state.rides, key = { it.id }) { ride ->
                                RideItemCard(
                                    ride = ride,
                                    onReserveClick = { viewModel.onReserveClicked(ride.id) },
                                    isReserveEnabled = !state.hasActiveRiderReservation &&
                                        !state.hasActiveDriverTrip &&
                                        ride.availableSlots > 0
                                )
                            }
                        }

                        state.hasActiveFilters -> item { EmptyFilteredStateView() }

                        else -> item { EmptyRidesStateView() }
                    }
                }
            }

            BottomNavigationBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                selectedTab = selectedBottomTab,
                onTabClick = {
                    selectedBottomTab = it
                    if (it == "Viajes") onTripsClick()
                }
            )
        }

        // Botón crear viaje (solo conductores)
        if (state.isDriver) {
            ExpandableCreateRideButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 116.dp),
                isBlocked = state.hasActiveDriverTrip || state.hasActiveRiderReservation || state.isOffline,
                blockedMessage = when {
                    state.isOffline -> "Necesitas internet para crear un viaje"
                    state.hasActiveDriverTrip && state.hasActiveRiderReservation -> "Ya tienes un viaje o reserva activa"
                    state.hasActiveDriverTrip -> "Ya tienes un viaje activo"
                    state.hasActiveRiderReservation -> "Ya tienes una reserva activa"
                    else -> ""
                },
                onCreateRideClick = onCreateRideClick
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composables de apoyo reutilizables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReservationMessageBanner(message: String, onDismiss: () -> Unit) {
    LaunchedEffect(message) {
        delay(3000)
        onDismiss()
    }
    val isSuccess = message.contains("correctamente")
    Text(
        text = message,
        color = if (isSuccess) DarkCyan else Color(0xFFEF4444),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSuccess) Color(0xFFE6FFFA) else Color(0xFFFFEDED),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cargando viajes...",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color(0xFFEF4444),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color(0xFFFFEDED), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpandableCreateRideButton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ExpandableCreateRideButton(
    modifier: Modifier = Modifier,
    isBlocked: Boolean = false,
    blockedMessage: String = "Ya tienes un viaje activo",
    onCreateRideClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
                    .clickable {
                        expanded = false
                        if (!isBlocked) onCreateRideClick()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isBlocked) blockedMessage else "Crear Viaje",
                    color = if (isBlocked) Color(0xFF94A3B8) else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (isBlocked) Color(0xFF374151) else Color(0xFF1F2937),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "+",
                color = if (isBlocked) Color(0xFF94A3B8) else Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HomeHeader
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeHeader(onLogoutClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBlue, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "HappyRide",
                tint = AutumnEmber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HappyRide",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = onLogoutClick, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Cerrar sesión",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OfertaViajestitle
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OfertaViajestitle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrussianBlue, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Oferta de viajes",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Encuentra el viaje perfecto para tu trayecto.",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FilterCard y FilterDropdownField
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FilterCard(
    selectedZone: String,
    zoneOptions: List<String>,
    selectedDay: String,
    selectedTripType: String,
    selectedDepartureTime: String,
    departureOptions: List<String>,
    hasActiveFilters: Boolean,
    activeFilterCount: Int,
    onZoneChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    onTripTypeChange: (String) -> Unit,
    onDepartureTimeChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    val dayOptions = listOf("Hoy", "Próximos viajes")
    val tripTypeOptions = listOf("Todos", "Hacia la universidad", "Desde la universidad")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrightSnow, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        FilterDropdownField(label = "Zona", selectedValue = selectedZone, options = zoneOptions, onValueSelected = onZoneChange)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                FilterDropdownField(
                    label = "Dia",
                    selectedValue = selectedDay,
                    options = dayOptions,
                    onValueSelected = onDayChange,
                    defaultValue = "Hoy",
                    neutralLabelColor = PrussianBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                FilterDropdownField(
                    label = "Tipo de viaje",
                    selectedValue = selectedTripType,
                    options = tripTypeOptions,
                    onValueSelected = onTripTypeChange
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FilterDropdownField(
            label = "Hora de salida",
            selectedValue = selectedDepartureTime,
            options = departureOptions,
            onValueSelected = onDepartureTimeChange,
            defaultValue = "Todas"
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hasActiveFilters) "$activeFilterCount filtros aplicados" else "Sin filtros aplicados",
                color = if (hasActiveFilters) DarkCyan else CoolSteel,
                style = MaterialTheme.typography.bodyMedium
            )
            if (hasActiveFilters) {
                Text(
                    text = "Limpiar",
                    color = DarkCyan,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onClearFilters() }
                )
            }
        }
    }
}

@Composable
fun FilterDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    useSelectionHighlight: Boolean = true,
    defaultValue: String = "Todos",
    neutralLabelColor: Color = Color.Unspecified
) {
    var expanded by remember { mutableStateOf(false) }
    val isActive = useSelectionHighlight && selectedValue != defaultValue
    val backgroundColor = if (isActive) Color(0xFFCCFBF1) else Color(0xFFF0F4F8)
    val borderColor = if (isActive) DarkCyan.copy(alpha = 0.35f) else Color.Transparent

    Text(
        text = label,
        color = PrussianBlue,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(text = selectedValue, style = MaterialTheme.typography.bodyMedium, color = PrussianBlue)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onValueSelected(option); expanded = false }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BottomNavigationBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedTab: String,
    onTabClick: (String) -> Unit
) {
    val items = listOf("Inicio", "Viajes")
    val icons = listOf(Icons.Default.Home, Icons.Default.LocalTaxi)

    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .background(headerBlue, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = item == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabClick(item) }.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = icons[index],
                    contentDescription = item,
                    tint = if (isSelected) AutumnEmber else CoolSteel,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item,
                    color = if (isSelected) AutumnEmber else CoolSteel,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}