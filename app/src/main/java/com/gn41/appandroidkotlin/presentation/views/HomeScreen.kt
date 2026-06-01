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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gn41.appandroidkotlin.presentation.components.RideItemCard
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val darkNavColor = Color(0xFF172033)
private val darkFabColor = Color(0xFF1E293B)
private val whiteCardColor = Color(0xFFF8FAFC)
private val darkTextColor = Color(0xFF0F172A)
private val secondaryTextColor = Color(0xFF475569)
private val orangeColor = Color(0xFFB45309)
private val successBackgroundColor = Color(0xFFD1FAE5)
private val successTextColor = Color(0xFF065F46)
private val errorBackgroundColor = Color(0xFFFEE2E2)
private val errorTextColor = Color(0xFFB91C1C)

// ─────────────────────────────────────────────────────────────────────────────
// Estados visuales para la zona de rides
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OfflineStateView() {
    EmptyStateCard(
        icon = Icons.Default.WifiOff,
        iconTint = MaterialTheme.colorScheme.primary,
        title = "Sin conexión a internet",
        message = "No podemos cargar los viajes ahora mismo.\nRevisa tu conexión e intenta de nuevo."
    )
}

@Composable
private fun EmptyRidesStateView() {
    EmptyStateCard(
        icon = Icons.Default.DirectionsCar,
        iconTint = orangeColor,
        title = "Sin viajes disponibles",
        message = "No hay viajes disponibles en este momento."
    )
}

@Composable
private fun EmptyFilteredStateView() {
    EmptyStateCard(
        icon = Icons.Default.SearchOff,
        iconTint = orangeColor,
        title = "Sin resultados",
        message = "Intenta cambiar los filtros para encontrar más viajes."
    )
}

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
                .background(darkNavColor, RoundedCornerShape(16.dp))
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
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.tertiary,
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
    onPagosClick: () -> Unit,
    onCreateRideClick: () -> Unit,
    onSettingsClick: () -> Unit
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (isLandscape) {
                HomeHeader(onSettingsClick = onSettingsClick)

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
                                preferredZoneName = state.preferredZoneName,
                                zoneOptions = state.zoneOptions,
                                selectedDate = state.selectedDate,
                                selectedTripType = state.selectedTripType,
                                selectedDepartureTime = state.selectedDepartureTime,
                                hasActiveFilters = state.hasActiveFilters,
                                activeFilterCount = state.activeFilterCount,
                                onZoneChange = viewModel::onZoneChange,
                                onDateChange = viewModel::onDateChange,
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
                    item { HomeHeader(onSettingsClick = onSettingsClick) }
                    item { OfertaViajestitle() }
                    item {
                        FilterCard(
                            selectedZone = state.selectedZone,
                            preferredZoneName = state.preferredZoneName,
                            zoneOptions = state.zoneOptions,
                            selectedDate = state.selectedDate,
                            selectedTripType = state.selectedTripType,
                            selectedDepartureTime = state.selectedDepartureTime,
                            hasActiveFilters = state.hasActiveFilters,
                            activeFilterCount = state.activeFilterCount,
                            onZoneChange = viewModel::onZoneChange,
                            onDateChange = viewModel::onDateChange,
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
                    if (it == "Viajes"){
                        onTripsClick()
                    }
                    else if (it == "Pagos"){
                        onPagosClick()
                    }
                }
            )
        }

        // Botón crear viaje (solo conductores)
        if (state.isDriver) {
            ExpandableCreateRideButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 116.dp),
                isBlocked = state.isCheckingBlockingState || state.hasActiveDriverTrip || state.hasActiveRiderReservation,
                blockedMessage = when{
                    state.isCheckingBlockingState -> "Validando tus viajes..."
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
        color = if (isSuccess) successTextColor else errorTextColor,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSuccess) successBackgroundColor else errorBackgroundColor,
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cargando viajes...",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
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
                    .background(darkFabColor, RoundedCornerShape(12.dp))
                    .clickable {
                        expanded = false
                        if (!isBlocked) onCreateRideClick()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isBlocked) blockedMessage else "Crear Viaje",
                    color = whiteCardColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .background(
                    color = if (isBlocked) darkFabColor.copy(alpha = 0.65f) else darkFabColor,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "+",
                color = if (isBlocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HomeHeader
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeHeader(onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(darkNavColor, shape = RoundedCornerShape(16.dp))
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HappyRide",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = onSettingsClick, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = MaterialTheme.colorScheme.onSurface,
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
            .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Oferta de viajes",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Encuentra el viaje perfecto para tu trayecto.",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FilterCard y FilterDropdownField
// ─────────────────────────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FilterCard(
    selectedZone: String,
    preferredZoneName: String,
    zoneOptions: List<String>,
    selectedDate: String,
    selectedTripType: String,
    selectedDepartureTime: String,
    hasActiveFilters: Boolean,
    activeFilterCount: Int,
    onZoneChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTripTypeChange: (String) -> Unit,
    onDepartureTimeChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val tripTypeOptions = listOf("Todos", "Hacia la universidad", "Desde la universidad")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCardColor, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        FilterDropdownField(
            label = "Zona",
            selectedValue = selectedZone,
            options = zoneOptions,
            onValueSelected = onZoneChange,
            defaultValue = preferredZoneName
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                FilterPickerField(
                    label = "Fecha",
                    selectedValue = formatDateForUi(selectedDate),
                    onClick = { showDatePicker = true },
                    isActive = selectedDate != todayDateString(),
                    neutralLabelColor = darkTextColor
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
        FilterPickerField(
            label = "Hora de salida",
            selectedValue = if (selectedDepartureTime == "Todas") "Todas" else selectedDepartureTime,
            onClick = { showTimePicker = true },
            isActive = selectedDepartureTime != "Todas"
        )
        if (selectedDepartureTime != "Todas") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Limpiar hora",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onDepartureTimeChange("Todas") }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hasActiveFilters) "$activeFilterCount filtros aplicados" else "Sin filtros aplicados",
                color = if (hasActiveFilters) MaterialTheme.colorScheme.secondary else secondaryTextColor,
                style = MaterialTheme.typography.bodyMedium
            )
            if (hasActiveFilters) {
                Text(
                    text = "Limpiar",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onClearFilters() }
                )
            }
        }
    }

    if (showDatePicker) {
        val utcTimeZone = TimeZone.getTimeZone("UTC")
        val minDateUtcMillis = remember {
            Calendar.getInstance(utcTimeZone).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val initialSelectedDateMillis = remember(selectedDate) {
            runCatching {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    isLenient = false
                    timeZone = utcTimeZone
                }.parse(selectedDate)?.time
            }.getOrNull()
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= minDateUtcMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                            timeZone = utcTimeZone
                        }.format(Date(millis))
                        onDateChange(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    if (showTimePicker) {
        val parsedHour = selectedDepartureTime.takeIf { it != "Todas" }
            ?.split(":")
            ?.getOrNull(0)
            ?.toIntOrNull()
            ?: 8
        val parsedMinute = selectedDepartureTime.takeIf { it != "Todas" }
            ?.split(":")
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: 0

        val timeState = rememberTimePickerState(
            initialHour = parsedHour,
            initialMinute = parsedMinute,
            is24Hour = true
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val time = String.format(Locale.getDefault(), "%02d:%02d", timeState.hour, timeState.minute)
                    onDepartureTimeChange(time)
                    showTimePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = { TimeInput(state = timeState) }
        )
    }
}

private fun formatDateForUi(date: String): String {
    val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
    val output = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val parsedDate = runCatching { input.parse(date) }.getOrNull() ?: return date
    return output.format(parsedDate)
}

private fun todayDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
    val filterTextColor = darkTextColor
    val backgroundColor = if (isActive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else filterTextColor.copy(alpha = 0.05f)
    val borderColor = if (isActive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f) else Color.Transparent

    Text(
        text = label,
        color = filterTextColor,
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
        Text(text = selectedValue, style = MaterialTheme.typography.bodyMedium, color = filterTextColor)
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
    val items = listOf("Inicio", "Viajes", "Pagos")
    val icons = listOf(Icons.Default.Home, Icons.Default.LocalTaxi, Icons.Default.AttachMoney)

    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .background(darkNavColor, RoundedCornerShape(16.dp))
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
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun FilterPickerField(
    label: String,
    selectedValue: String,
    onClick: () -> Unit,
    isActive: Boolean,
    neutralLabelColor: Color = Color.Unspecified
) {
    val backgroundColor = if (isActive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val borderColor = if (isActive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f) else Color.Transparent
    val filterTextColor = darkTextColor

    Text(
        text = label,
        color = if (neutralLabelColor == Color.Unspecified) filterTextColor else neutralLabelColor,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(if (isActive) backgroundColor else filterTextColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = selectedValue,
            style = MaterialTheme.typography.bodyMedium,
            color = filterTextColor
        )
    }
}
