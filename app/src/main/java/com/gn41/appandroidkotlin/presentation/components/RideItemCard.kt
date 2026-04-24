package com.gn41.appandroidkotlin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.presentation.viewmodels.RideItemUiModel
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber
import com.gn41.appandroidkotlin.ui.theme.BrightSnow
import com.gn41.appandroidkotlin.ui.theme.CoolSteel
import com.gn41.appandroidkotlin.ui.theme.DarkCyan
import com.gn41.appandroidkotlin.ui.theme.PrussianBlue

private val CardDivider = Color(0xFFE2E8F0)

@Composable
fun RideItemCard(
    ride: RideItemUiModel,
    onReserveClick: () -> Unit = {},
    isReserveEnabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrightSnow, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // conductor y precio arriba
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            DriverInfoSection(
                name = ride.driverName,
                rating = ride.driverRating,
                cancellationRiskPercent = ride.cancellationRiskPercent
            )
            PriceSection(price = ride.price)
        }

        Spacer(modifier = Modifier.height(12.dp))
        CardDividerLine()
        Spacer(modifier = Modifier.height(12.dp))

        // ruta, fecha y tipo
        RideRouteSection(ride = ride)

        Spacer(modifier = Modifier.height(12.dp))
        CardDividerLine()
        Spacer(modifier = Modifier.height(10.dp))

        // vehiculo, cupos y boton
        RideActionSection(
            availableSlots = ride.availableSlots,
            zoneName = ride.zoneName,
            isReserveEnabled = isReserveEnabled,
            onReserveClick = { if (isReserveEnabled) showDialog = true }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirmar reserva") },
            text = { Text(text = "¿Deseas reservar este viaje?") },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "Cancelar")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReserveClick()
                        showDialog = false
                    }
                ) {
                    Text(text = "Confirmar")
                }
            }
        )
    }
}

@Composable
private fun DriverInfoSection(
    name: String,
    rating: String?,
    cancellationRiskPercent: Int?
) {
    val ratingNumber = rating?.toDoubleOrNull()
    val ratingValue = if (ratingNumber != null) {
        String.format("%.1f⭐", ratingNumber)
    } else {
        "Sin calificacion"
    }
    val ratingColor = ratingSemaphoreColor(rating)
    val riskValue = cancellationRiskPercent?.let { "${it}%" } ?: "Sin viajes"
    val riskColor = cancellationRiskSemaphoreColor(cancellationRiskPercent)

    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = PrussianBlue
        )
        Spacer(modifier = Modifier.height(2.dp))
        LabeledValueText(
            label = "Calificacion",
            value = ratingValue,
            color = PrussianBlue,
            valueColor = ratingColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        LabeledValueText(
            label = "Riesgo de cancelacion",
            value = riskValue,
            color = PrussianBlue,
            valueColor = riskColor
        )
    }
}

@Composable
private fun PriceSection(price: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = price,
            style = MaterialTheme.typography.titleMedium,
            color = AutumnEmber,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "por cupo",
            style = MaterialTheme.typography.bodyMedium,
            color = CoolSteel
        )
    }
}

@Composable
private fun RideRouteSection(ride: RideItemUiModel) {
    // origen y destino
    Text(
        text = "Origen: ${ride.source}",
        style = MaterialTheme.typography.titleMedium,
        color = PrussianBlue
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
        text = "Destino: ${ride.destination}",
        style = MaterialTheme.typography.titleMedium,
        color = PrussianBlue
    )

    Spacer(modifier = Modifier.height(6.dp))

    // fecha y hora
    Row {
        LabeledValueText(label = "Fecha", value = formatDateText(ride.date), color = PrussianBlue)
        Spacer(modifier = Modifier.width(16.dp))
        LabeledValueText(label = "Salida", value = formatTimeText(ride.departureTime), color = PrussianBlue)
    }

    Spacer(modifier = Modifier.height(8.dp))

    val (badgeBackground, badgeText) = rideTypeBadgeColors(ride.type)

    // tipo de viaje en badge
    Box(
        modifier = Modifier
            .background(badgeBackground, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Tipo: ") }
                append(mapRideTypeLabel(ride.type))
            },
            style = MaterialTheme.typography.bodyMedium,
            color = badgeText,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun rideTypeBadgeColors(type: String): Pair<Color, Color> {
    return when (type) {
        "TO_UNIVERSITY" -> Color(0xFFE6FFFA) to DarkCyan
        "FROM_UNIVERSITY" -> Color(0xFFFEF3C7) to AutumnEmber
        else -> Color(0xFFE2E8F0) to PrussianBlue
    }
}

@Composable
private fun RideActionSection(
    availableSlots: Int,
    zoneName: String,
    isReserveEnabled: Boolean = true,
    onReserveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // vehiculo, cupos, zona
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            LabeledValueText(label = "Cupos disponibles", value = availableSlots.toString(), color = PrussianBlue)
            if (zoneName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                LabeledValueText(label = "Zona", value = zoneName, color = PrussianBlue)
            }
        }

        // boton reservar — gris cuando está bloqueado
        Box(
            modifier = Modifier
                .widthIn(min = 92.dp)
                .background(
                    color = if (isReserveEnabled) AutumnEmber else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(enabled = isReserveEnabled) { onReserveClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Reservar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun CardDividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CardDivider)
    )
}

private fun mapRideTypeLabel(type: String): String {
    return when (type) {
        "TO_UNIVERSITY" -> "Hacia la universidad"
        "FROM_UNIVERSITY" -> "Desde la universidad"
        else -> type
    }
}

@Composable
private fun LabeledValueText(
    label: String,
    value: String,
    color: Color = PrussianBlue,
    valueColor: Color = color
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = color)) { append("$label: ") }
            withStyle(SpanStyle(color = valueColor)) { append(value) }
        },
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

private fun ratingSemaphoreColor(rating: String?): Color {
    val ratingNumber = rating?.toDoubleOrNull() ?: return CoolSteel
    return when {
        ratingNumber in 1.0..<2.0 -> Color(0xFFDC2626)
        ratingNumber in 2.0..<3.0 -> Color(0xFFF97316)
        ratingNumber in 3.0..<4.0 -> Color(0xFFEAB308)
        ratingNumber in 4.0..5.0 -> Color(0xFF16A34A)
        else -> CoolSteel
    }
}

private fun cancellationRiskSemaphoreColor(riskPercent: Int?): Color {
    val risk = riskPercent ?: return CoolSteel
    return when {
        risk in 0..24 -> Color(0xFF16A34A)
        risk in 25..49 -> Color(0xFFEAB308)
        risk in 50..74 -> Color(0xFFF97316)
        risk in 75..100 -> Color(0xFFDC2626)
        else -> CoolSteel
    }
}

private fun formatDateText(rawDate: String): String {
    val parts = rawDate.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else rawDate
}

private fun formatTimeText(rawTime: String): String {
    val parts = rawTime.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else rawTime
}