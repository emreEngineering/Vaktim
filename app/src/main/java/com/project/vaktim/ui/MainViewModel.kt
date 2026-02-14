package com.project.vaktim.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.vaktim.core.AppDefaults
import com.project.vaktim.data.local.LocationPreferences
import com.project.vaktim.data.repository.IQuotesRepository
import com.project.vaktim.di.AppContainer
import com.project.vaktim.domain.GetPrayerDashboardUseCase
import com.project.vaktim.domain.model.LocationSelection
import com.project.vaktim.ui.state.UiState
import com.project.vaktim.util.PrayerTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val getPrayerDashboardUseCase: GetPrayerDashboardUseCase,
    private val quotesRepository: IQuotesRepository,
    private val locationPreferences: LocationPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var isInitialized = false

    fun loadInitialData() {
        if (isInitialized) return
        isInitialized = true

        val savedLocation = locationPreferences.getSavedLocation()
        loadPrayerTimes(savedLocation)
        loadDailyQuote()
    }

    fun onLocationChanged(city: String, country: String, district: String = "") {
        val location = LocationSelection(city = city, country = country, district = district)
        locationPreferences.saveLocation(location)
        loadPrayerTimes(location)
    }

    fun loadPrayerTimes(location: LocationSelection = currentLocation()) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    city = location.city,
                    country = location.country,
                    district = location.district,
                    error = null
                )
            }

            getPrayerDashboardUseCase(location).fold(
                onSuccess = { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            prayerTimes = dashboard.prayerTimes,
                            nextPrayer = dashboard.nextPrayer,
                            remainingTime = dashboard.remainingTime,
                            hijriDate = dashboard.hijriDate,
                            gregorianDate = dashboard.gregorianDate,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: AppDefaults.UNKNOWN_ERROR
                        )
                    }
                }
            )
        }
    }

    fun loadDailyQuote(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.quote != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isQuoteLoading = true, quoteError = null) }
            quotesRepository.getRandomVerse().fold(
                onSuccess = { quote ->
                    _uiState.update { it.copy(isQuoteLoading = false, quote = quote, quoteError = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isQuoteLoading = false,
                            quote = null,
                            quoteError = error.message ?: AppDefaults.UNKNOWN_ERROR
                        )
                    }
                }
            )
        }
    }

    fun updateRemainingTime() {
        val prayerList = _uiState.value.prayerTimes
        if (prayerList.isEmpty()) return

        val (nextPrayer, remaining) = PrayerTimeUtils.calculateNextPrayer(prayerList)
        _uiState.update {
            it.copy(
                nextPrayer = nextPrayer,
                remainingTime = remaining
            )
        }
    }

    private fun currentLocation(): LocationSelection {
        val state = _uiState.value
        return LocationSelection(
            city = state.city,
            country = state.country,
            district = state.district
        )
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        return MainViewModel(
                            getPrayerDashboardUseCase = container.getPrayerDashboardUseCase,
                            quotesRepository = container.quotesRepository,
                            locationPreferences = container.locationPreferences
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
