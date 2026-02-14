package com.project.vaktim.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.vaktim.data.model.DailyQuote
import com.project.vaktim.ui.theme.GoldLight
import com.project.vaktim.ui.theme.GoldMuted
import com.project.vaktim.ui.theme.NavySurface
import com.project.vaktim.ui.theme.TextWhite

@Composable
fun DailyQuotesSection(
    isLoading: Boolean,
    quote: DailyQuote?,
    errorMessage: String?
) {
    when {
        isLoading -> QuoteLoadingCard()
        quote != null -> VerseCard(quote = quote)
        else -> ErrorCard(message = errorMessage ?: "Ayet yuklenemedi.")
    }
}

@Composable
private fun QuoteLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = GoldLight)
            Text(
                text = "Gunluk ayet yukleniyor...",
                color = TextWhite,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun VerseCard(quote: DailyQuote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Gunluk Ayet",
                color = GoldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            quote.arabicText?.takeIf { it.isNotBlank() }?.let { arabic ->
                Text(
                    text = arabic,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = quote.text,
                color = TextWhite,
                fontSize = 15.sp
            )
            Text(
                text = quote.source,
                color = GoldMuted,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Text(
            text = message,
            color = GoldMuted,
            modifier = Modifier.padding(16.dp)
        )
    }
}
