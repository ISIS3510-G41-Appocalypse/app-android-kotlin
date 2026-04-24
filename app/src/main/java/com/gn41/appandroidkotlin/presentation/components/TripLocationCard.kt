package com.gn41.appandroidkotlin.presentation.components

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import com.gn41.appandroidkotlin.domain.UserSharedLocation
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

private val TripLocationCardBackground = Color(0xFF3A3946)
private val TripLocationPrimaryText = Color(0xFFD6D6E0)
private val TripLocationSecondaryText = Color(0xFFB8B8C7)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(MapboxExperimental::class)
@Composable
fun TripLocationCard(
    isDriver: Boolean,
    isLocationSharingEnabled: Boolean,
    onToggleLocationSharing: (Boolean) -> Unit,
    hasLocationPermission: Boolean,
    currentLatitude: Double?,
    currentLongitude: Double?,
    sharedUsersCount: Int,
    totalUsersInRide: Int,
    rideLocations: List<UserSharedLocation>,
    currentUserId: Int
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

    val userPoint = if (currentLatitude != null && currentLongitude != null) {
        Point.fromLngLat(currentLongitude, currentLatitude)
    } else {
        null
    }

    val mapViewportState = rememberMapViewportState()

    LaunchedEffect(userPoint, isLocationSharingEnabled) {
        if (userPoint != null && isLocationSharingEnabled) {
            mapViewportState.setCameraOptions {
                center(userPoint)
                zoom(16.0)
            }
        }
    }

    val latestLocations = rideLocations
        .filter { it.isSharingEnabled }
        .groupBy { it.userId }
        .mapNotNull { (_, locations) -> locations.maxByOrNull { it.timestamp } }
        .filter { it.userId != currentUserId }

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
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "+",
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                mapViewportState.easeTo(
                                    CameraOptions.Builder()
                                        .zoom(mapViewportState.cameraState?.zoom?.plus(1.0) ?: 16.0)
                                        .build()
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    Text(
                        text = "-",
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                mapViewportState.easeTo(
                                    CameraOptions.Builder()
                                        .zoom(mapViewportState.cameraState?.zoom?.minus(1.0) ?: 14.0)
                                        .build()
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
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
                text = if (hasLocationPermission) "Permiso: concedido" else "Permiso: no concedido",
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