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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.gn41.appandroidkotlin.ui.theme.PrussianBlue

private val CardDivider = Color(0xFFE2E8F0)
private val TypeBadgeBg = Color(0xFFFEF3C7)

@Composable
fun RideItemCard(ride: RideItemUiModel) {
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
            DriverInfoSection(name = ride.driverName, rating = ride.driverRating)
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
            vehicleInfo = ride.vehicleInfo,
            totalSlots = ride.totalSlots,
            zoneName = ride.zoneName
        )
    }
}

@Composable
private fun DriverInfoSection(name: String, rating: String?) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = PrussianBlue
        )
        if (rating != null) {
            Spacer(modifier = Modifier.height(2.dp))
            LabeledValueText(label = "Calificacion", value = rating, color = PrussianBlue)
        }
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

    // tipo de viaje en badge
    Box(
        modifier = Modifier
            .background(TypeBadgeBg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Tipo: ") }
                append(mapRideTypeLabel(ride.type))
            },
            style = MaterialTheme.typography.bodyMedium,
            color = AutumnEmber,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RideActionSection(
    vehicleInfo: String,
    totalSlots: String,
    zoneName: String
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
            LabeledValueText(label = "Vehiculo", value = vehicleInfo, color = PrussianBlue)
            if (totalSlots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                LabeledValueText(label = "Cupos", value = totalSlots, color = PrussianBlue)
            }
            if (zoneName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                LabeledValueText(label = "Zona", value = zoneName, color = PrussianBlue)
            }
        }

        // boton reservar
        Box(
            modifier = Modifier
                .widthIn(min = 92.dp)
                .background(AutumnEmber, RoundedCornerShape(10.dp))
                .clickable { /* TODO: implement reservation flow */ }
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
    color: Color = PrussianBlue
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = color)) { append("$label: ") }
            append(value)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = color
    )
}

private fun formatDateText(rawDate: String): String {
    val parts = rawDate.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else rawDate
}

private fun formatTimeText(rawTime: String): String {
    val parts = rawTime.split(":")
    return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else rawTime
}