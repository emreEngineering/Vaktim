package com.project.vaktim.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.vaktim.data.model.PrayerTime
import com.project.vaktim.ui.theme.*

@Composable
fun NextPrayerCard(
    nextPrayer: PrayerTime,
    remainingTime: String,
    hijriDate: String,
    gregorianDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GoldPrimary, GoldLight)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sonraki Vakit: ${nextPrayer.turkishName}",
                    color = MidnightNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = remainingTime,
                    color = MidnightNavy,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MidnightNavy.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Miladi", color = MidnightNavy.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(text = gregorianDate, color = MidnightNavy.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Hicri", color = MidnightNavy.copy(alpha = 0.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(text = hijriDate, color = MidnightNavy.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
