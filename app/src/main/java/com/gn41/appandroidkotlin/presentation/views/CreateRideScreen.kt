package com.gn41.appandroidkotlin.presentation.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel
import com.gn41.appandroidkotlin.presentation.viewmodels.CreateRideViewModel.CreateRideUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
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

    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState) {
        if (uiState is CreateRideUiState.Success) {
            onBackClick()
        }
    }

    if (viewModel.isLoadingData) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBus,
                            contentDescription = "Bus",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                .background(MaterialTheme.colorScheme.background)
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Completa los detalles para compartir tu ruta con la comunidad.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
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
                    modifier = Modifier.fillMaxWidth()
                        .pointerInput(Unit){
                            detectTapGestures(onTap = {
                                focusManager.clearFocus()
                            })
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
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
                                    Icon(Icons.Default.DirectionsCar, null, tint = MaterialTheme.colorScheme.onSurface)
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
                                    Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.onSurface)
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
                                    Icon(Icons.Default.Sell, null, tint = MaterialTheme.colorScheme.onSurface)
                                },
                                onClick = { expandedType = true }
                            )

                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false }
                            ) {
                                viewModel.rideTypes.asSequence().forEach { type ->
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
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
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
                                Icon(Icons.Default.Flag, null, tint = MaterialTheme.colorScheme.secondary)
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
                                Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colorScheme.onSurface)
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
                                    },
                                    enabled = { true }
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                SectionLabel("HORA DE SALIDA")
                                SmallSelectionField(
                                    text = formState.departureTime.ifEmpty { "Selecciona" },
                                    icon = Icons.Default.AccessTime,
                                    onClick = {
                                        viewModel.clearTimeValidationMessage()
                                        showTimePicker = true
                                    },
                                    enabled = {
                                        if (formState.date.isEmpty()){
                                            false
                                        }
                                        else{
                                            true
                                        }
                                    }
                                )
                            }
                        }

                        if (viewModel.timeValidationMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.timeValidationMessage,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                            showTimePicker = false
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
                                containerColor = MaterialTheme.colorScheme.primary,
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
                                Text("Viaje publicado", color = MaterialTheme.colorScheme.secondary)
                            }

                            else -> {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    if (showDatePicker) {
        val todayMillis = remember {
            LocalDate.now()
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }

        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayMillis
                }
            },
            yearRange = IntRange(LocalDate.now().year, LocalDate.now().year)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()


                        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        val formattedDate = date.format(format)

                        viewModel.onDateSelected(formattedDate)
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

                    viewModel.validateAndSetDepartureTime(time)

                    if (viewModel.timeValidationMessage.isEmpty()) {
                        showTimePicker = false
                    }
                }) {
                    Text("Aceptar")
                }
            },
            text = {
                TimeInput(state = timeState)
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
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
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(modifier = Modifier.width(10.dp))
            if (text == "Selecciona tu zona" || text == "Selecciona tu vehículo" || text == "Selecciona tipo"){
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            else
            {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.weight(1f)
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SmallSelectionField(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: () -> Boolean
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        enabled = enabled(),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
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
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (text == "Selecciona"){
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
            }
            else
            {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 13.sp
                )
            }
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
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        maxLines = 1,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.background,
            unfocusedTextColor = MaterialTheme.colorScheme.background,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun BottomNavBar(
    onBackClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = Color.Transparent
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onBackClick,
            icon = { Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary) },
            label = { Text("Volver", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary) }
        )
    }
}