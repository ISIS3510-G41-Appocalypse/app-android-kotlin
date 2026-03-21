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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel.CreateRideUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val DarkBlueTop = Color(0xFF040B1F)
private val DarkBlueBottom = Color(0xFF101A36)
private val AccentOrange = Color(0xFFB85A0C)
private val SoftCard = Color(0xFFF7F7F8)
private val BorderGray = Color(0xFFD8DCE3)
private val TextGray = Color(0xFF8A93A5)
private val SuccessCard = Color(0xFF0C2830)
private val SuccessIcon = Color(0xFF19B38A)
private val WhiteText = Color(0xFFF7F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRideScreen(
    viewModel: CreateRideViewModel,
    onBackClick: () -> Unit
) {
    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedZone by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val formState = viewModel.formState
    val uiState = viewModel.uiState

    LaunchedEffect(uiState) {
        if (uiState is CreateRideUiState.Success) {
            onBackClick()
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(DarkBlueTop, DarkBlueBottom)
    )

    if (viewModel.isLoadingData) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentOrange)
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "HappyRide",
                        color = AccentOrange,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBus,
                            contentDescription = "Bus",
                            tint = AccentOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = WhiteText
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ofertar viaje",
                    color = WhiteText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Completa los detalles para compartir tu ruta con la\ncomunidad.",
                    color = Color(0xFFC4CAD7),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (viewModel.loadErrorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.loadErrorMessage,
                        color = Color(0xFFFCA5A5),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        SectionLabel("VEHÍCULO")
                        Box {
                            SelectionField(
                                text = viewModel.vehicles
                                    .find { it.licensePlate == viewModel.formState.vehicleId }
                                    ?.licensePlate ?: "Selecciona tu vehículo",
                                leadingIcon = {
                                    Icon(Icons.Default.DirectionsCar, null, tint = TextGray)
                                },
                                onClick = { expandedVehicle = true }
                            )

                            DropdownMenu(
                                expanded = expandedVehicle,
                                onDismissRequest = { expandedVehicle = false }
                            ) {
                                viewModel.vehicles.forEach { vehicle ->
                                    DropdownMenuItem(
                                        text = { Text(vehicle.licensePlate) },
                                        onClick = {
                                            viewModel.onVehicleSelected(vehicle.licensePlate)
                                            expandedVehicle = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SectionLabel("ZONA")
                        Box {
                            SelectionField(
                                text = viewModel.zones
                                    .find { it.name == viewModel.formState.zoneId }
                                    ?.name ?: "Selecciona tu zona",
                                leadingIcon = {
                                    Icon(Icons.Default.Place, null, tint = TextGray)
                                },
                                onClick = { expandedZone = true }
                            )

                            DropdownMenu(
                                expanded = expandedZone,
                                onDismissRequest = { expandedZone = false }
                            ) {
                                viewModel.zones.forEach { zone ->
                                    DropdownMenuItem(
                                        text = { Text(zone.name) },
                                        onClick = {
                                            viewModel.onZoneSelected(zone.name)
                                            expandedZone = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SectionLabel("TIPO")
                        Box {
                            SelectionField(
                                text = viewModel.formState.type.ifEmpty { "Selecciona tipo" },
                                leadingIcon = {
                                    Icon(Icons.Default.Sell, null, tint = TextGray)
                                },
                                onClick = { expandedType = true }
                            )

                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false }
                            ) {
                                viewModel.rideTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            viewModel.onTypeSelected(type)
                                            expandedType = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SectionLabel("INICIO")
                        CustomOutlinedField(
                            value = formState.source,
                            onValueChange = {
                                viewModel.onSourceChanged(it)
                            },
                            placeholder = "Punto de salida",
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, null, tint = AccentOrange)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SectionLabel("DESTINO")
                        CustomOutlinedField(
                            value = formState.destination,
                            onValueChange = {
                                viewModel.onDestinationChanged(it)
                            },
                            placeholder = "Destino final",
                            leadingIcon = {
                                Icon(Icons.Default.Flag, null, tint = SuccessIcon)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SectionLabel("PRECIO")
                        CustomOutlinedField(
                            value = formState.price,
                            onValueChange = {
                                viewModel.onPriceChanged(it)
                            },
                            placeholder = "Precio por pasajero",
                            leadingIcon = {
                                Icon(Icons.Default.Sell, null, tint = TextGray)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                SectionLabel("FECHA")
                                SmallSelectionField(
                                    text = formState.date.ifEmpty { "Selecciona" },
                                    icon = Icons.Default.DateRange,
                                    onClick = {
                                        showDatePicker = true
                                    }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                SectionLabel("HORA DE SALIDA")
                                SmallSelectionField(
                                    text = formState.departureTime.ifEmpty { "00:00" },
                                    icon = Icons.Default.AccessTime,
                                    onClick = {
                                        showTimePicker = true
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.createRide()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Publicar viaje",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null
                            )
                        }
                        when (uiState) {
                            is CreateRideUiState.Loading -> {
                                CircularProgressIndicator()
                            }

                            is CreateRideUiState.Error -> {
                                Text(uiState.message, color = Color.Red)
                            }

                            is CreateRideUiState.Success -> {
                                Text("Viaje publicado", color = SuccessIcon)
                            }

                            else -> {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                SecurityBanner()

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->

                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis

                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = format.format(calendar.time)

                        viewModel.onDateSelected(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showTimePicker) {
        val timeState = rememberTimePickerState(is24Hour = true)

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val time = String.format(
                        Locale.US,
                        "%02d:%02d:00",
                        timeState.hour,
                        timeState.minute
                    )

                    viewModel.onDepartureTimeSelected(time)
                    showTimePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            text = {
                TimePicker(state = timeState)
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SelectionField(
    text: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF1F3F6),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3E7EE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = Color(0xFF4E5665),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextGray
            )
        }
    }
}

@Composable
private fun SmallSelectionField(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF7F8FA),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = TextGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CustomOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFFB1B8C5)
            )
        },
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF7F8FA),
            unfocusedContainerColor = Color(0xFFF7F8FA),
            focusedBorderColor = Color(0xFF6C7485),
            unfocusedBorderColor = Color(0xFFB8BFCA),
            focusedTextColor = Color(0xFF263041),
            unfocusedTextColor = Color(0xFF263041),
            cursorColor = AccentOrange
        )
    )
}

@Composable
private fun SecurityBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF103943)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SuccessIcon,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Tu oferta será visible para todos los estudiantes en tu ruta.",
                color = Color(0xFF8EDFD0),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    onBackClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = Color(0xFF081126)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onBackClick,
            icon = { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") },
            label = { Text("Volver", fontSize = 10.sp) }
        )
    }
}