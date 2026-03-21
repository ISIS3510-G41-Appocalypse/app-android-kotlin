package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.presentation.components.RideItemCard
import com.gn41.appandroidkotlin.presentation.viewmodels.HomeViewModel
import kotlinx.coroutines.delay

val darkBlue = Color(0xFF0B1E3B)
val whiteCard = Color(0xFFF5F7FA)
val selectedBottomItemColor = Color(0xFF0D9488)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTripsClick: () -> Unit,
    onCreateRideClick: () -> Unit
) {
    val state = viewModel.uiState
    var selectedBottomTab by remember { mutableStateOf("Inicio") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            HomeHeader()

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            if (state.reservationMessage.isNotEmpty()) {
                LaunchedEffect(state.reservationMessage) {
                    delay(3000)
                    viewModel.clearReservationMessage()
                }

                val isSuccess = state.reservationMessage.contains("correctamente")
                Text(
                    text = state.reservationMessage,
                    color = if (isSuccess) Color(0xFF0D9488) else Color(0xFFB45309),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSuccess) Color(0xFFE6FFFA) else Color(0xFFFEF3C7),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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

                state.errorMessage.isNotEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                state.rides.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.rides) { ride ->
                            RideItemCard(
                                ride = ride,
                                onReserveClick = { viewModel.onReserveClicked(ride.id) },
                                isReserveEnabled = !state.hasActiveRiderReservation && !state.hasActiveDriverTrip
                            )
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.hasActiveFilters) {
                                "No hay viajes para los filtros seleccionados."
                            } else {
                                "No hay viajes disponibles en este momento."
                            },
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BottomNavigationBar(
                selectedTab = selectedBottomTab,
                onTabClick = {
                    selectedBottomTab = it
                    if (it == "Viajes") onTripsClick()
                }
            )
        }

        ExpandableCreateRideButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 84.dp),
            isBlocked = state.hasActiveDriverTrip,
            onCreateRideClick = onCreateRideClick
        )
    }
}

@Composable
fun ExpandableCreateRideButton(
    modifier: Modifier = Modifier,
    isBlocked: Boolean = false,
    onCreateRideClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
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
                    text = if (isBlocked) "Ya tienes un viaje activo" else "Crear Viaje",
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

@Composable
fun HomeHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HappyRide",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Oferta de viajes",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Encuentra el viaje perfecto para tu trayecto.",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

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
    val dayOptions = listOf("Todos", "Hoy")
    val tripTypeOptions = listOf("Todos", "Hacia la universidad", "Desde la universidad")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        FilterDropdownField(
            label = "Zona",
            selectedValue = selectedZone,
            options = zoneOptions,
            onValueSelected = onZoneChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                FilterDropdownField(
                    label = "Dia",
                    selectedValue = selectedDay,
                    options = dayOptions,
                    onValueSelected = onDayChange
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
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
            onValueSelected = onDepartureTimeChange
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (hasActiveFilters) "$activeFilterCount filtros aplicados" else "Sin filtros aplicados",
                color = if (hasActiveFilters) Color(0xFF0D9488) else Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            if (hasActiveFilters) {
                Text(
                    text = "Limpiar",
                    color = Color(0xFF0D9488),
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
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isActive = selectedValue != "Todos" && selectedValue != "Todas"
    val backgroundColor = if (isActive) Color(0xFFDDEAFE) else Color(0xFFE5E7EB)
    val borderColor = if (isActive) Color(0xFF93C5FD) else Color.Transparent

    Text(
        text = label,
        color = if (isActive) Color(0xFF1E3A8A) else Color.Unspecified,
        style = MaterialTheme.typography.titleMedium
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
        Text(
            text = selectedValue,
            style = MaterialTheme.typography.bodyMedium
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabClick: (String) -> Unit
) {
    val items = listOf("Inicio", "Viajes")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedTab
            Text(
                text = item,
                color = if (isSelected) selectedBottomItemColor else Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable { onTabClick(item) }
                    .background(
                        if (isSelected) Color(0xFFE6FFFA) else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}