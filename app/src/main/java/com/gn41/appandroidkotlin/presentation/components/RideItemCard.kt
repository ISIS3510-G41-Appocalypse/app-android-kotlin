package com.gn41.appandroidkotlin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.data.dto.rides.RideDto

@Composable
fun RideItemCard(ride: RideDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Route: source → destination
        Text(
            text = "${ride.source}  →  ${ride.destination}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Date: ${ride.date}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Departure: ${ride.departure_time}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Type: ${ride.type}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

