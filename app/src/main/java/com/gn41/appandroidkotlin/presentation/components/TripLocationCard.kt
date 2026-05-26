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
import androidx.compose.material3.Button
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
import com.gn41.appandroidkotlin.presentation.viewmodels.MapUserMarkerUiState
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions


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
    currentUserId: Int,
    isUsingCachedLocations: Boolean,
    cachedLocationMessage: String,
    onRefreshLocations: () -> Unit,
    mapMarkers: List<MapUserMarkerUiState>,
    isOfflineMode: Boolean = false
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

    val fallbackPoint = rideLocations
        .firstOrNull()
        ?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        }

    val mapViewportState = rememberMapViewportState()

    LaunchedEffect(
        userPoint,
        fallbackPoint,
        isLocationSharingEnabled
    ) {

        val targetPoint = userPoint ?: fallbackPoint

        if (targetPoint != null) {
            mapViewportState.setCameraOptions {
                center(targetPoint)
                zoom(16.0)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Ubicación del viaje",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
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
                ) {
                    mapMarkers.forEach { marker ->
                        val markerPoint = Point.fromLngLat(
                            marker.longitude,
                            marker.latitude
                        )

                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(markerPoint)
                                allowOverlap(true)
                            }
                        ) {
                            Text(
                                text = if (marker.isCurrentUser) {
                                    "Tú"
                                } else if (marker.distanceMeters != null) {
                                    "U${marker.userId} · ${marker.distanceMeters} m"
                                } else {
                                    "U${marker.userId}"
                                },
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .background(
                                        if (marker.isCurrentUser)
                                            MaterialTheme.colorScheme.secondary
                                        else
                                            MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable {
                                        mapViewportState.easeTo(
                                            CameraOptions.Builder()
                                                .center(markerPoint)
                                                .zoom(16.0)
                                                .build()
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (isUsingCachedLocations) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp)
                    ) {

                        CachedLocationBanner(
                            message = cachedLocationMessage
                        )
                    }
                }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "+",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
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
                            text = "-", color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
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
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            Text(
                text = roleMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
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
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isLocationSharingEnabled,
                    onCheckedChange = if (isOfflineMode) null else onToggleLocationSharing,
                    enabled = !isOfflineMode
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )

            if (isOfflineMode) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No disponible en modo offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasLocationPermission) "Permiso: concedido" else "Permiso: no concedido",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(lineSpacing))

            Text(
                text = if (currentLatitude != null && currentLongitude != null) {
                    "Ubicación disponible"
                } else {
                    "Ubicación no disponible"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(lineSpacing))

            Text(
                text = "Usuarios compartiendo: $sharedUsersCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(lineSpacing))

            Text(
                text = "Usuarios en el ride: $totalUsersInRide",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }


@Composable
private fun CachedLocationBanner(
    message: String
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {

        Text(
            text = message.ifBlank {
                "Mostrando últimas ubicaciones conocidas. Las posiciones no se actualizarán hasta recuperar conexión."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}