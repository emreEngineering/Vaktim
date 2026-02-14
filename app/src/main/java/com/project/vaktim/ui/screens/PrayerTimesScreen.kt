package com.project.vaktim.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.vaktim.di.AppGraph
import com.project.vaktim.ui.MainViewModel
import com.project.vaktim.ui.components.DailyQuotesSection
import com.project.vaktim.ui.components.LocationSelector
import com.project.vaktim.ui.components.NextPrayerCard
import com.project.vaktim.ui.components.PrayerTimeCard
import com.project.vaktim.ui.theme.GoldPrimary
import com.project.vaktim.ui.theme.MidnightNavy
import kotlinx.coroutines.delay

@Composable
fun PrayerTimesScreen(
    onServiceStart: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val container = remember(context.applicationContext) { AppGraph.from(context.applicationContext) }
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModel.provideFactory(container)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            viewModel.updateRemainingTime()
        }
    }

    Scaffold(containerColor = MidnightNavy) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LocationSelector(
                currentCity = uiState.city,
                currentCountry = uiState.country,
                currentDistrict = uiState.district,
                onLocationChanged = { city, country, district ->
                    viewModel.onLocationChanged(city, country, district)
                    onServiceStart(city, country, district)
                }
            )

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }

                uiState.error != null -> {
                    Text(text = "Hata: ${uiState.error}", color = Color(0xFFEF5350))
                }

                else -> {
                    uiState.nextPrayer?.let { next ->
                        NextPrayerCard(
                            nextPrayer = next,
                            remainingTime = uiState.remainingTime,
                            hijriDate = uiState.hijriDate,
                            gregorianDate = uiState.gregorianDate
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.prayerTimes, key = { it.name }) { prayer ->
                            PrayerTimeCard(prayer = prayer, isNext = prayer == uiState.nextPrayer)
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            DailyQuotesSection(
                                isLoading = uiState.isQuoteLoading,
                                quote = uiState.quote,
                                errorMessage = uiState.quoteError
                            )
                        }
                    }
                }
            }
        }
    }
}
