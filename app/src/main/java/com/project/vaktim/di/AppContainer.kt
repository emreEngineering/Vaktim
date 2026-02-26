package com.project.vaktim.di

import android.content.Context
import com.project.vaktim.data.local.LocationPreferences
import com.project.vaktim.data.local.PreferencesManager
import com.project.vaktim.data.local.PrayerTimesOfflineStore
import com.project.vaktim.data.local.PrayerTimesPreferencesStore
import com.project.vaktim.data.repository.IPrayerTimesRepository
import com.project.vaktim.data.repository.IQuotesRepository
import com.project.vaktim.data.repository.PrayerTimesRepository
import com.project.vaktim.data.repository.QuotesRepository
import com.project.vaktim.domain.GetPrayerDashboardUseCase

class AppContainer(context: Context) {

    val locationPreferences: LocationPreferences = PreferencesManager(context.applicationContext)
    val prayerTimesOfflineStore: PrayerTimesOfflineStore =
        PrayerTimesPreferencesStore(context.applicationContext)
    val prayerTimesRepository: IPrayerTimesRepository =
        PrayerTimesRepository(offlineStore = prayerTimesOfflineStore)
    val quotesRepository: IQuotesRepository = QuotesRepository()
    val getPrayerDashboardUseCase: GetPrayerDashboardUseCase =
        GetPrayerDashboardUseCase(prayerTimesRepository)
}

object AppGraph {
    @Volatile
    private var container: AppContainer? = null

    fun from(context: Context): AppContainer {
        return container ?: synchronized(this) {
            container ?: AppContainer(context.applicationContext).also { container = it }
        }
    }
}
