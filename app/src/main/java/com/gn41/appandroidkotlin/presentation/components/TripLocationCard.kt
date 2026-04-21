package com.gn41.appandroidkotlin.presentation.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val TripLocationCardBackground = Color(0xFF3A3946)
private val TripLocationPrimaryText = Color(0xFFD6D6E0)
private val TripLocationSecondaryText = Color(0xFFB8B8C7)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TripLocationCard(
    isDriver: Boolean,
    isLocationSharingEnabled: Boolean,
    onToggleLocationSharing: (Boolean) -> Unit,
    hasLocationPermission: Boolean,
    currentLatitude: Double?,
    currentLongitude: Double?,
    sharedUsersCount: Int,
    totalUsersInRide: Int
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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

    val mapHeight = if (isLandscape) 110.dp else 300.dp
    val topSpacing = if (isLandscape) 10.dp else 14.dp
    val sectionSpacing = if (isLandscape) 10.dp else 16.dp
    val roleSpacing = if (isLandscape) 10.dp else 18.dp
    val lineSpacing = if (isLandscape) 2.dp else 4.dp

    val cameraPositionState = rememberCameraPositionState()

    val userLocation = if (currentLatitude != null && currentLongitude != null) {
        LatLng(currentLatitude, currentLongitude)
    } else {
        null
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TripLocationCardBackground)
                .padding(16.dp)
        ) {
            Text(
                text = "Ubicación del viaje",
                style = MaterialTheme.typography.titleMedium,
                color = TripLocationPrimaryText
            )

            Spacer(modifier = Modifier.height(topSpacing))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxWidth(),
                    cameraPositionState = cameraPositionState
                ) {
                    userLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Tu ubicación"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            Text(
                text = roleMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TripLocationPrimaryText
            )

            Spacer(modifier = Modifier.height(roleSpacing))

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = TripLocationSecondaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasLocationPermission) {
                    "Permiso: concedido"
                } else {
                    "Permiso: no concedido"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(lineSpacing))

            Text(
                text = if (currentLatitude != null && currentLongitude != null) {
                    "Ubicación disponible"
                } else {
                    "Ubicación no disponible"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(lineSpacing))

            Text(
                text = "Usuarios compartiendo: $sharedUsersCount",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(lineSpacing))

            Text(
                text = "Usuarios en el ride: $totalUsersInRide",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
    }
}