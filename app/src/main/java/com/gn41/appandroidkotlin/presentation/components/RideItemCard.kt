package com.gn41.appandroidkotlin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.data.dto.rides.RideDto
import com.gn41.appandroidkotlin.ui.theme.AutumnEmber
import com.gn41.appandroidkotlin.ui.theme.BrightSnow
import com.gn41.appandroidkotlin.ui.theme.CoolSteel
import com.gn41.appandroidkotlin.ui.theme.PrussianBlue

@Composable
fun RideItemCard(ride: RideDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrightSnow, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "${ride.source}  →  ${ride.destination}",
            style = MaterialTheme.typography.titleMedium,
            color = PrussianBlue
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Date: ${formatDateText(ride.date)}",
            style = MaterialTheme.typography.bodyMedium,
            color = CoolSteel
        )

        Text(
            text = "Departure: ${formatTimeText(ride.departure_time)}",
            style = MaterialTheme.typography.bodyMedium,
            color = CoolSteel
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(
                text = "Type:",
                style = MaterialTheme.typography.bodyMedium,
                color = CoolSteel
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = mapRideTypeLabel(ride.type),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AutumnEmber
            )
        }
    }
}

private fun mapRideTypeLabel(type: String): String {
    return when (type) {
        "TO_UNIVERSITY" -> "To university"
        "FROM_UNIVERSITY" -> "From university"
        else -> type
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