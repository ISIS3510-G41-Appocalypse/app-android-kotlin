package com.gn41.appandroidkotlin.presentation.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

private val TripLocationCardBackground = Color(0xFF3A3946)
private val TripLocationMapPlaceholder = Color(0xFF9AA9BE)
private val TripLocationPrimaryText = Color(0xFFD6D6E0)
private val TripLocationSecondaryText = Color(0xFFB8B8C7)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TripLocationCard(
    isDriver: Boolean,
    isLocationSharingEnabled: Boolean,
    onToggleLocationSharing: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(TripLocationCardBackground)
                .padding(16.dp)
        ) {
            val roleMessage = if (isDriver) {
                "Aquí podrás ver a tus riders cuando compartan ubicación."
            } else {
                "Aquí podrás ver al conductor y tu posición relativa."
            }

            val statusText = if (isLocationSharingEnabled) {
                "Ubicación compartida"
            } else {
                "Ubicación oculta"
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(0.42f)
                    ) {
                        Text(
                            text = "Ubicación del viaje",
                            style = MaterialTheme.typography.titleMedium,
                            color = TripLocationPrimaryText
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = roleMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TripLocationPrimaryText
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Compartir mi ubicación",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TripLocationPrimaryText,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Switch(
                                checked = isLocationSharingEnabled,
                                onCheckedChange = onToggleLocationSharing
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TripLocationSecondaryText
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .weight(0.58f)
                            .height(320.dp)
                            .background(
                                color = TripLocationMapPlaceholder,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mapa del viaje",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ubicación del viaje",
                        style = MaterialTheme.typography.titleMedium,
                        color = TripLocationPrimaryText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                color = TripLocationMapPlaceholder,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mapa del viaje",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = roleMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TripLocationPrimaryText
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compartir mi ubicación",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TripLocationPrimaryText,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = isLocationSharingEnabled,
                            onCheckedChange = onToggleLocationSharing
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TripLocationSecondaryText
                    )
                }
            }
        }
    }
}