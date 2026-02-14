package com.project.vaktim.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.project.vaktim.core.AppDefaults
import com.project.vaktim.data.local.LocationPreferences
import com.project.vaktim.data.model.PrayerTime
import com.project.vaktim.di.AppGraph
import com.project.vaktim.domain.GetPrayerDashboardUseCase
import com.project.vaktim.domain.model.LocationSelection
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PrayerTimesService : Service() {

    companion object {
        private const val TAG = "PrayerTimesService"
        const val EXTRA_CITY = "city"
        const val EXTRA_COUNTRY = "country"
        const val EXTRA_DISTRICT = "district"

        fun startService(context: Context, city: String, country: String, district: String = "") {
            val intent = Intent(context, PrayerTimesService::class.java).apply {
                putExtra(EXTRA_CITY, city)
                putExtra(EXTRA_COUNTRY, country)
                putExtra(EXTRA_DISTRICT, district)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, PrayerTimesService::class.java))
        }
    }

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var locationPreferences: LocationPreferences
    private lateinit var getPrayerDashboardUseCase: GetPrayerDashboardUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var updateJob: Job? = null

    private var location = LocationSelection(
        city = AppDefaults.DEFAULT_CITY,
        country = AppDefaults.DEFAULT_COUNTRY
    )
    private var lastLoadedDate: LocalDate? = null
    private var prayerTimes: List<PrayerTime> = emptyList()

    override fun onCreate() {
        super.onCreate()
        val container = AppGraph.from(this)
        notificationHelper = NotificationHelper(this)
        locationPreferences = container.locationPreferences
        getPrayerDashboardUseCase = container.getPrayerDashboardUseCase
        notificationHelper.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        location = readLocation(intent)

        startForegroundWithType(
            NotificationHelper.NOTIFICATION_ID,
            notificationHelper.createSimpleNotification("Namaz vakitleri yukleniyor...")
        )

        startUpdating()
        return START_STICKY
    }

    private fun readLocation(intent: Intent?): LocationSelection {
        val savedLocation = locationPreferences.getSavedLocation()
        return LocationSelection(
            city = intent?.getStringExtra(EXTRA_CITY) ?: savedLocation.city,
            country = intent?.getStringExtra(EXTRA_COUNTRY) ?: savedLocation.country,
            district = intent?.getStringExtra(EXTRA_DISTRICT) ?: savedLocation.district
        )
    }

    private fun startForegroundWithType(notificationId: Int, notification: Notification) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                startForeground(
                    notificationId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            }

            else -> startForeground(notificationId, notification)
        }
    }

    private fun startUpdating() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                if (shouldReloadPrayerTimes()) {
                    loadPrayerTimes()
                } else {
                    updateNotification()
                }
                delay(millisUntilNextMinute())
            }
        }
    }

    private fun shouldReloadPrayerTimes(): Boolean {
        return prayerTimes.isEmpty() || lastLoadedDate != LocalDate.now()
    }

    private suspend fun loadPrayerTimes() {
        getPrayerDashboardUseCase(location).fold(
            onSuccess = { dashboard ->
                prayerTimes = dashboard.prayerTimes
                lastLoadedDate = LocalDate.now()
                updateNotification()
            },
            onFailure = { error ->
                Log.e(TAG, "Prayer times could not be loaded", error)
                notificationHelper.notify(
                    notificationHelper.createSimpleNotification("Namaz vakitleri yuklenemedi")
                )
            }
        )
    }

    private fun updateNotification() {
        if (prayerTimes.isEmpty()) return
        notificationHelper.notify(notificationHelper.createCustomNotification(prayerTimes))
    }

    private fun millisUntilNextMinute(): Long {
        val now = System.currentTimeMillis()
        val remainder = now % 60_000
        return if (remainder == 0L) 60_000L else 60_000L - remainder
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
    }
}
