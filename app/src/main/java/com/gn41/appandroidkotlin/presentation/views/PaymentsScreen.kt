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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.PaymentMethodDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.presentation.viewmodels.PaymentsViewModel
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber

private val whiteCardColor = Color(0xFFF8FAFC)
private val darkTextColor = Color(0xFF0F172A)
private val secondaryTextColor = Color(0xFF475569)
private val darkNavColor = Color(0xFF172033)
private val orangeColor = Color(0xFFB45309)
private val fieldBorderColor = Color(0xFFCBD5E1)

private val warningBackgroundColor = Color(0xFFFEF3C7)

private val warningTextColor = Color(0xFF92400E)

@Composable
fun PaymentsScreen(
    viewModel: PaymentsViewModel,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {

                Text(
                    text = "Mis pagos",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {

                Text(
                    text = "Consulta tu información de pagos como conductor o pasajero.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            item {

                PaymentSectionSwitch(
                    enable = !viewModel.isLoadingData,
                    selectedSection = viewModel.selectedRole,
                    onSectionSelected = {
                        viewModel.onRoleChange(it)
                    }
                )
            }

            if (viewModel.monto != -1) {
                if (viewModel.selectedRole == "Conductor") {
                    item {
                        Text(
                            text = "Te deben en total: $${viewModel.monto}",
                            style = MaterialTheme.typography.titleMedium,
                            color = orangeColor
                        )
                    }
                }
                else{
                    item {
                        Text(
                            text = "Debes en total: $${viewModel.monto}",
                            style = MaterialTheme.typography.titleMedium,
                            color = orangeColor
                        )
                    }
                }
            }

            if (viewModel.isLoadingData) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                if (!viewModel.connectivity){
                    item{
                        OfflineInfoBanner(
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
                if (viewModel.rides.isEmpty()) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    item {
                        Text(
                            text = "No tienes pagos pendientes",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    if (viewModel.selectedRole == "Conductor") {

                        items(
                            items = viewModel.rides,
                            key = { it.id }
                        ) { ride ->

                            DriverPaymentCard(
                                viewModel,
                                ride = ride,
                                payments = viewModel.payments.getOrDefault(ride.id, emptyList())
                            )
                        }

                    } else {

                        items(viewModel.rides) { ride ->

                            RiderPaymentCard(
                                viewModel,
                                ride = ride,
                                payments = viewModel.payments.getOrDefault(ride.id, emptyList())
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }

        BottomNavigationBar(
            selectedTab = "Pagos",
            onTabClick = {
                if (it == "Inicio") {
                    onHomeClick()
                } else if (it == "Viajes") {
                    onTripsClick()
                }
            }
        )
    }
}

@Composable
private fun DriverPaymentCard(
    viewModel: PaymentsViewModel,
    ride: RidePaymentDto,
    payments: List<PaymentDto>
) {;
    val pendientes : String = if (payments.size==1){
        "1 pasajero no ha pagado"
    }
    else
    {
        "${payments.size} pasajeros no han pagado"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                whiteCardColor,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = pendientes,
            style = MaterialTheme.typography.titleMedium,
            color = orangeColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${ride.source} → ${ride.destination}",
            style = MaterialTheme.typography.titleMedium,
            color = darkTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Fecha: ${ride.date}",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )

        Text(
            text = "Hora: ${ride.departureTime}",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pagos pendientes",
            style = MaterialTheme.typography.titleSmall,
            color = darkTextColor
        )

        payments.forEach { payment ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${payment.firstName} ${payment.lastName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = darkTextColor
                        )

                        Text(
                            text = "Pago pendiente",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor
                        )
                    }

                    Text(
                        text = "$ ${payment.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AutumnEmber
                    )
                }

                if (payment.state == "POR CONFIRMAR") {

                    Text(
                        text = "El usuario ha pagado. Confirma el pago.",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        SmallActionButton(
                            text = "Confirmar",
                            onClick = {
                                viewModel.onConfirmarPago(payment.id)
                            },
                            enabled = viewModel.connectivity,
                            accentColor = MaterialTheme.colorScheme.secondary
                        )

                        SmallActionButton(
                            text = "Rechazar",
                            onClick = {
                                viewModel.onRechazarPago(payment.id)
                            },
                            enabled = viewModel.connectivity,
                            accentColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RiderPaymentCard(
    viewModel: PaymentsViewModel,
    ride: RidePaymentDto,
    payments: List<PaymentDto>,
) {

    val payment = payments.firstOrNull()

    var expanded by remember {
        mutableStateOf(false)
    }

    var selectedMethod by remember {
        mutableStateOf<PaymentMethodDto?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                whiteCardColor,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "${ride.source} → ${ride.destination}",
            style = MaterialTheme.typography.titleMedium,
            color = darkTextColor
        )

        Text(
            text = "Fecha: ${ride.date}",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )

        Text(
            text = "Hora: ${ride.departureTime}",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Monto pendiente",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )

        Text(
            text = "$ ${payment?.amount ?: 0}",
            style = MaterialTheme.typography.titleLarge,
            color = AutumnEmber
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (payment?.state == "POR CONFIRMAR"){
            Text(
                text = "En espera de confirmación.",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor
            )
        }
        else {
            Text(
                text = "Método de pago",
                style = MaterialTheme.typography.bodyMedium,
                color = darkTextColor
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {

                OutlinedTextField(
                    value = selectedMethod?.let {
                        it.methodName
                    } ?: "Selecciona un método",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = whiteCardColor,
                        unfocusedContainerColor = whiteCardColor,
                        focusedTextColor = darkTextColor,
                        unfocusedTextColor = darkTextColor,
                        focusedIndicatorColor = darkTextColor,
                        unfocusedIndicatorColor = fieldBorderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    payment?.paymentMethods?.forEach { method ->

                        DropdownMenuItem(
                            text = {
                                Column {

                                    Text(
                                        text = method.methodName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            },
                            onClick = {
                                selectedMethod = method
                                expanded = false
                            }
                        )
                    }
                }
            }

            SmallActionButton(
                text = "Pagar",
                onClick = {
                    viewModel.onPayClicked(payment?.id ?: -1, selectedMethod?.methodName ?: "")
                },
                enabled = selectedMethod != null && viewModel.connectivity,
                accentColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun PaymentSectionSwitch(
    enable: Boolean,
    selectedSection: String,
    onSectionSelected: (String) -> Unit
) {

    val items = listOf(
        "Conductor",
        "Pasajero"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                darkNavColor,
                RoundedCornerShape(12.dp)
            )
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items.forEach { item ->

            val selected = item == selectedSection

            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    Color(0xFFCBD5E1)
                },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) {
                            orangeColor
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = enable) {
                        onSectionSelected(item)
                    }
                    .padding(vertical = 10.dp)
                    .padding(horizontal = 12.dp)
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

    val backgroundColor =
        if (enabled) {
            accentColor.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    Box(
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (enabled) {
                    accentColor
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                },
                RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled) {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                accentColor
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
fun OfflineInfoBanner(
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                warningBackgroundColor,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sin conexión. Mostrando la última información disponible.",
            style = MaterialTheme.typography.bodySmall,
            color = warningTextColor,
            maxLines = 2
        )
    }
}
