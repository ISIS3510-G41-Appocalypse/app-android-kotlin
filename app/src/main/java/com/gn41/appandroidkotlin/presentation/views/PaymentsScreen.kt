package com.gn41.appandroidkotlin.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber

@Composable
fun PaymentsScreen(onHomeClick: () -> Unit, onTripsClick: () -> Unit) {

    var selectedSection by remember {
        mutableStateOf("Conductor")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        item {

            PaymentSectionSwitch(
                selectedSection = selectedSection,
                onSectionSelected = {
                    selectedSection = it
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
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