package com.project.vaktim.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.vaktim.data.model.PrayerTime
import com.project.vaktim.ui.theme.*

@Composable
fun PrayerTimeCard(prayer: PrayerTime, isNext: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) GoldPrimary.copy(alpha = 0.2f) else NavyCard.copy(alpha = 0.62f)
        ),
        shape = RoundedCornerShape(18.dp),
        border = if (isNext) BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.85f))
        else BorderStroke(1.dp, GlassBorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prayer.turkishName,
                color = if (isNext) GoldLight else TextWhite.copy(alpha = 0.97f),
                fontSize = 17.sp,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = prayer.time,
                color = if (isNext) GoldLight else TextWhite.copy(alpha = 0.97f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
