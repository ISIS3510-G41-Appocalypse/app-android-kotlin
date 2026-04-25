package com.gn41.appandroidkotlin.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gn41.appandroidkotlin.data.dto.createRide.RideUserDto

@Composable
fun RideUserCard(
    user: RideUserDto,
    modifier: Modifier = Modifier,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    val oddsPercentage = (user.cancellationOdds * 100).toInt()

    val oddsColor = when {
        oddsPercentage < 30 -> Color(0xFF2E7D32) // verde
        oddsPercentage < 70 -> Color(0xFFF9A825) // amarillo
        else -> Color(0xFFC62828) // rojo
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.name} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Probabilidad de cancelar: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.background
                    )

                    Text(
                        text = "$oddsPercentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = oddsColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row {
                IconButton(
                    onClick = { onReject() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rechazar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { onAccept() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aceptar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RiderCard(
    user: RideUserDto,
    modifier: Modifier = Modifier,
) {
    val oddsPercentage = (user.cancellationOdds * 100).toInt()

    val oddsColor = when {
        oddsPercentage < 30 -> Color(0xFF2E7D32) // verde
        oddsPercentage < 70 -> Color(0xFFF9A825) // amarillo
        else -> Color(0xFFC62828) // rojo
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.name} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Probabilidad de cancelar: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.background
                    )

                    Text(
                        text = "$oddsPercentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = oddsColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}