package com.project.vaktim.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.project.vaktim.MainActivity
import com.project.vaktim.R
import com.project.vaktim.data.model.PrayerTime
import com.project.vaktim.di.AppGraph
import com.project.vaktim.domain.model.LocationSelection
import com.project.vaktim.util.PrayerTimeUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerTimesAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        refreshFromRepository(context, appWidgetIds, goAsync())
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(componentName(context))
            refreshFromRepository(context, ids, goAsync())
        }
    }

    companion object {
        private const val ACTION_WIDGET_REFRESH = "com.project.vaktim.widget.ACTION_WIDGET_REFRESH"
        private val TR_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM EEEE", TR_LOCALE)
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private val PRIMARY_TEXT = Color.parseColor("#1D1D1D")
        private val SECONDARY_TEXT = Color.parseColor("#333333")
        private val HIGHLIGHT_TEXT = Color.parseColor("#C17930")

        fun requestUpdate(context: Context) {
            val refreshIntent = Intent(context, PrayerTimesAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            context.sendBroadcast(refreshIntent)
        }

        fun updateFromPrayerTimes(
            context: Context,
            location: LocationSelection,
            prayerTimes: List<PrayerTime>
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val widgetIds = manager.getAppWidgetIds(componentName(context))
            if (widgetIds.isEmpty()) return

            val dateLabel = LocalDate.now().format(DATE_FORMATTER)
            val (nextPrayer, _) = PrayerTimeUtils.calculateNextPrayer(prayerTimes)
            val summary = if (nextPrayer != null) {
                "$dateLabel - ${nextPrayer.turkishName}: ${nextPrayer.time}"
            } else {
                dateLabel
            }

            widgetIds.forEach { widgetId ->
                manager.updateAppWidget(
                    widgetId,
                    buildContentRemoteViews(
                        context = context,
                        location = location,
                        summary = summary,
                        prayerTimes = prayerTimes
                    )
                )
            }
        }

        private fun refreshFromRepository(
            context: Context,
            widgetIds: IntArray,
            pendingResult: PendingResult
        ) {
            if (widgetIds.isEmpty()) {
                pendingResult.finish()
                return
            }

            val appContext = context.applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val container = AppGraph.from(appContext)
                    val location = container.locationPreferences.getSavedLocation()
                    val manager = AppWidgetManager.getInstance(appContext)

                    container.getPrayerDashboardUseCase(location).fold(
                        onSuccess = { dashboard ->
                            val summary = buildString {
                                append(dashboard.gregorianDate)
                                dashboard.nextPrayer?.let {
                                    append(" - ")
                                    append(it.turkishName)
                                    append(": ")
                                    append(it.time)
                                }
                            }
                            widgetIds.forEach { widgetId ->
                                manager.updateAppWidget(
                                    widgetId,
                                    buildContentRemoteViews(
                                        context = appContext,
                                        location = location,
                                        summary = summary,
                                        prayerTimes = dashboard.prayerTimes
                                    )
                                )
                            }
                        },
                        onFailure = {
                            widgetIds.forEach { widgetId ->
                                manager.updateAppWidget(
                                    widgetId,
                                    buildErrorRemoteViews(
                                        context = appContext,
                                        location = location
                                    )
                                )
                            }
                        }
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }

        private fun buildContentRemoteViews(
            context: Context,
            location: LocationSelection,
            summary: String,
            prayerTimes: List<PrayerTime>
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.home_widget_prayer_times)
            views.setTextViewText(R.id.widgetLocation, locationLabel(location))
            views.setTextViewText(R.id.widgetSummary, summary)
            views.setImageViewResource(R.id.widgetLogo, R.drawable.logo)

            val nameIds = listOf(
                R.id.widgetName1,
                R.id.widgetName2,
                R.id.widgetName3,
                R.id.widgetName4,
                R.id.widgetName5,
                R.id.widgetName6
            )
            val timeIds = listOf(
                R.id.widgetTime1,
                R.id.widgetTime2,
                R.id.widgetTime3,
                R.id.widgetTime4,
                R.id.widgetTime5,
                R.id.widgetTime6
            )

            val activePrayerName = activePrayerName(prayerTimes)
            nameIds.indices.forEach { index ->
                val prayer = prayerTimes.getOrNull(index)
                if (prayer != null) {
                    val isActive = prayer.name == activePrayerName
                    views.setTextViewText(nameIds[index], prayer.turkishName)
                    views.setTextViewText(timeIds[index], prayer.time)
                    views.setTextColor(nameIds[index], if (isActive) HIGHLIGHT_TEXT else PRIMARY_TEXT)
                    views.setTextColor(timeIds[index], if (isActive) HIGHLIGHT_TEXT else SECONDARY_TEXT)
                } else {
                    views.setTextViewText(nameIds[index], "--")
                    views.setTextViewText(timeIds[index], "--:--")
                    views.setTextColor(nameIds[index], PRIMARY_TEXT)
                    views.setTextColor(timeIds[index], SECONDARY_TEXT)
                }
            }

            bindIntents(context, views)
            return views
        }

        private fun buildErrorRemoteViews(
            context: Context,
            location: LocationSelection
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.home_widget_prayer_times)
            views.setTextViewText(R.id.widgetLocation, locationLabel(location))
            views.setTextViewText(R.id.widgetSummary, context.getString(R.string.widget_error))
            views.setImageViewResource(R.id.widgetLogo, R.drawable.logo)

            val nameIds = listOf(
                R.id.widgetName1,
                R.id.widgetName2,
                R.id.widgetName3,
                R.id.widgetName4,
                R.id.widgetName5,
                R.id.widgetName6
            )
            val timeIds = listOf(
                R.id.widgetTime1,
                R.id.widgetTime2,
                R.id.widgetTime3,
                R.id.widgetTime4,
                R.id.widgetTime5,
                R.id.widgetTime6
            )
            nameIds.indices.forEach { index ->
                views.setTextViewText(nameIds[index], "--")
                views.setTextViewText(timeIds[index], "--:--")
                views.setTextColor(nameIds[index], PRIMARY_TEXT)
                views.setTextColor(timeIds[index], SECONDARY_TEXT)
            }

            bindIntents(context, views)
            return views
        }

        private fun bindIntents(context: Context, views: RemoteViews) {
            val openAppIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, openAppIntent)

            val refreshIntent = Intent(context, PrayerTimesAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widgetRefresh, refreshPendingIntent)
        }

        private fun activePrayerName(
            prayerTimes: List<PrayerTime>,
            now: LocalTime = LocalTime.now()
        ): String? {
            val mainPrayers = prayerTimes.filter { it.name != "Sunrise" }
            if (mainPrayers.isEmpty()) return null

            var activeName: String? = null
            mainPrayers.forEach { prayer ->
                runCatching { LocalTime.parse(prayer.time, TIME_FORMATTER) }
                    .onSuccess { prayerTime ->
                        if (!prayerTime.isAfter(now)) {
                            activeName = prayer.name
                        }
                    }
            }

            return activeName ?: mainPrayers.lastOrNull()?.name
        }

        private fun componentName(context: Context): ComponentName {
            return ComponentName(context, PrayerTimesAppWidgetProvider::class.java)
        }

        private fun locationLabel(location: LocationSelection): String {
            return location.district.ifBlank { location.city }
        }
    }
}
