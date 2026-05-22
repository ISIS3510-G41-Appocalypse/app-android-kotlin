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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.data.dto.payments.PaymentDto
import com.gn41.appandroidkotlin.data.dto.payments.RidePaymentDto
import com.gn41.appandroidkotlin.presentation.viewmodels.PaymentsViewModel
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber

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
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
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

                item {

                    PaymentSectionSwitch(
                        selectedSection = viewModel.selectedRole,
                        onSectionSelected = {
                            viewModel.onRoleChange(it)
                        }
                    )
                }

                if (viewModel.selectedRole == "Conductor") {

                    items(
                        items = viewModel.rides,
                        key = { it.id }
                    ) { ride ->

                        DriverPaymentCard(
                            ride = ride,
                            payments = viewModel.payments.getOrDefault(ride.id, emptyList())
                        )
                    }

                } else {

                    items(viewModel.rides) { ride ->

                        RiderPaymentCard(
                            ride = ride,
                            payments = viewModel.payments.getOrDefault(ride.id, emptyList()),
                            onPayClick = {
                                viewModel.onPayClicked()
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
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
    ride: RidePaymentDto,
    payments: List<PaymentDto>
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "${ride.source} → ${ride.destination}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Fecha: ${ride.date}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Hora: ${ride.departureTime}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pagos pendientes",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        payments.forEach { payment ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "${payment.firstName} ${payment.lastName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Pago pendiente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Text(
                    text = "$ ${payment.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = AutumnEmber
                )
            }
        }
    }
}

@Composable
private fun RiderPaymentCard(
    ride: RidePaymentDto,
    payments: List<PaymentDto>,
    onPayClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "${ride.source} → ${ride.destination}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Fecha: ${ride.date}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Hora: ${ride.departureTime}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Monto pendiente",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary
        )

        Text(
            text = "$ ${payments.first().amount}",
            style = MaterialTheme.typography.titleLarge,
            color = AutumnEmber
        )

        SmallActionButton(
            text = "Pagar",
            onClick = onPayClick,
            accentColor = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun PaymentSectionSwitch(
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
                MaterialTheme.colorScheme.surface,
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
                    MaterialTheme.colorScheme.tertiary
                },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) {
                            AutumnEmber
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
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