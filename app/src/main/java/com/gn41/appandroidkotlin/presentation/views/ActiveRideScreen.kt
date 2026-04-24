package com.gn41.appandroidkotlin.presentation.views

import android.util.Log
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gn41.appandroidkotlin.data.dto.createRide.ActiveRideDto
import com.gn41.appandroidkotlin.data.dto.createRide.RideUserDto
import com.gn41.appandroidkotlin.presentation.components.RideUserCard
import com.gn41.appandroidkotlin.presentation.viewmodels.ActiveRideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRideScreen(
    viewModel: ActiveRideViewModel,
    onBackClick: () -> Unit
){
    Scaffold(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
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
            BottomNavigationBar(
                selectedTab = "Viajes",
                onTabClick = {
                    if (it == "Inicio") onBackClick()
                }
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
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mis viajes",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Gestiona tus viajes activos",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                RoleSelector(
                    selectedRole = viewModel.selectedRole,
                    onRoleSelected = { viewModel.onSelectedRole(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                when(viewModel.selectedRole){
                    "Conductor" -> {
                        if (viewModel.ride == null)
                        {
                            Text(
                                text = "No hay viajes activos",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp
                            )
                        }
                        else{
                            RideCard(ride = viewModel.ride!!,
                                onCancelClick = { id ->
                                    viewModel.onCancelarViaje(id)
                                })
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text="Reservas aprobadas",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(text="Solicitudes de reservas",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (viewModel.rideUsers != null){
                                RideUserList(users = viewModel.rideUsers!!,
                                    onAccept = {},
                                    onReject = {})
                            }
                            else
                            {
                                Text(
                                    text = "No hay solicitudes de reserva",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    "Pasajero" ->{
                        Text("...",color = MaterialTheme.colorScheme.secondary)
                    }
                    "" -> {
                        Text("......", color=MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun RideCard(
    ride: ActiveRideDto,
    modifier: Modifier = Modifier,
    onCancelClick: (Int) -> Unit = {},
    onStartClick: (ActiveRideDto) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

                Text(
                    text = "${ride.source} → ${ride.destination}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.background,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fecha: ${ride.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )

                Text(
                    text = "Hora salida: ${ride.departureTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )

                Text(
                    text = "Precio: $${ride.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Estado: ${ride.state}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = {
                        if (ride.type=="FROM_UNIVERSITY")
                        {
                            "Tipo: Desde la universidad"
                        }
                        else
                        {
                            "Tipo: Hacia la universidad"
                        }
                    }(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    OutlinedButton(
                        onClick = { onCancelClick(ride.id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.background
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { onStartClick(ride) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Iniciar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
            }
        }
    }
}

@Composable
fun RideUserList(
    users: List<RideUserDto>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    LazyColumn{
        items(users) { user ->
            RideUserCard(
                user = user,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                onAccept = onAccept,
                onReject = onReject
            )
        }
    }
}

@Composable
fun RoleSelector(
    selectedRole: String?,
    onRoleSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        RoleButton(
            text = "Conductor",
            selected = selectedRole == "Conductor",
            onClick = { onRoleSelected("Conductor") },
            modifier = Modifier.weight(1f)
        )

        RoleButton(
            text = "Pasajero",
            selected = selectedRole == "Pasajero",
            onClick = { onRoleSelected("Pasajero") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun RoleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = colors
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text)
        }
    }
}