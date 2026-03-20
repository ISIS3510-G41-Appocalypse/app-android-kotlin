package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.background
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

val darkBlue = Color(0xFF0B1E3B)
val whiteCard = Color(0xFFF5F7FA)
val selectedBottomItemColor = Color(0xFF0D9488)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateRideClick: () -> Unit
) {
    val state = viewModel.uiState
    var selectedBottomTab by remember { mutableStateOf("Home") }

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
                selectedDay = state.selectedDay,
                selectedTripType = state.selectedTripType,
                selectedDepartureTime = state.selectedDepartureTime,
                departureOptions = state.departureTimeOptions,
                onDayChange = viewModel::onDayChange,
                onTripTypeChange = viewModel::onTripTypeChange,
                onDepartureTimeChange = viewModel::onDepartureTimeChange
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                                text = "Loading rides...",
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
                            RideItemCard(ride = ride)
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
                            text = "No rides available right now.",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BottomNavigationBar(
                selectedTab = selectedBottomTab,
                onTabClick = { selectedBottomTab = it }
            )
        }

        ExpandableCreateRideButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 84.dp),
            onCreateRideClick = onCreateRideClick
        )
    }
}

@Composable
fun ExpandableCreateRideButton(
    modifier: Modifier = Modifier,
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
                        onCreateRideClick()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Crear Viaje",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .background(Color(0xFF1F2937), RoundedCornerShape(10.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "+",
                color = Color.White,
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
    selectedDay: String,
    selectedTripType: String,
    selectedDepartureTime: String,
    departureOptions: List<String>,
    onDayChange: (String) -> Unit,
    onTripTypeChange: (String) -> Unit,
    onDepartureTimeChange: (String) -> Unit
) {
    val dayOptions = listOf("All", "Today")
    val tripTypeOptions = listOf("All", "To university", "From university")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteCard, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Zone",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "Colina",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                FilterDropdownField(
                    label = "Day",
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
                    label = "Trip type",
                    selectedValue = selectedTripType,
                    options = tripTypeOptions,
                    onValueSelected = onTripTypeChange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        FilterDropdownField(
            label = "Departure time",
            selectedValue = selectedDepartureTime,
            options = departureOptions,
            onValueSelected = onDepartureTimeChange
        )
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

    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
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
    val items = listOf("Home", "Viajes")

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