package com.project.vaktim

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import com.project.vaktim.data.local.PreferencesManager
import com.project.vaktim.domain.model.LocationSelection
import com.project.vaktim.service.PrayerTimesService
import com.project.vaktim.ui.screens.PrayerTimesScreen
import com.project.vaktim.ui.theme.VaktimTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private var pendingServiceStart: LocationSelection? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingServiceStart?.let(::startPrayerTimesService)
        }
        pendingServiceStart = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        val savedLocation = preferencesManager.getSavedLocation()

        setContent {
            VaktimTheme {
                PrayerTimesScreen(
                    onServiceStart = { city, country, district ->
                        startServiceWithPermission(LocationSelection(city, country, district))
                    }
                )
            }
        }

        startServiceWithPermission(savedLocation)
    }

    private fun startServiceWithPermission(location: LocationSelection) {
        if (hasNotificationPermission()) {
            startPrayerTimesService(location)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingServiceStart = location
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun startPrayerTimesService(location: LocationSelection) {
        PrayerTimesService.startService(
            context = this,
            city = location.city,
            country = location.country,
            district = location.district
        )
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
