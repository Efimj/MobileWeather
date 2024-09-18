package com.yefim.mobileweatherapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yefim.mobileweatherapp.R
import com.yefim.mobileweatherapp.ui.components.CustomFAB
import com.yefim.mobileweatherapp.ui.components.LocationCard
import com.yefim.mobileweatherapp.ui.components.VerticalSpacer
import com.yefim.mobileweatherapp.ui.helper.LocaleProvider.LocalInsetsPaddings
import com.yefim.mobileweatherapp.ui.modifier.fadingEdges
import com.yefim.mobileweatherapp.ui.theme.MobileWeatherAppTheme
import com.yefim.mobileweatherapp.util.settings.NightMode
import com.yefim.mobileweatherapp.util.settings.SettingsManager
import com.yefim.mobileweatherapp.util.settings.SettingsManager.settings
import com.yefim.mobileweatherapp.widget.WeatherWidget.Companion.updateWeatherWidgets

class ConfigureActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        SettingsManager.init(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        actionBar?.hide()
        super.onCreate(savedInstanceState)

        val appWidgetId = getWidgetId()
        if (appWidgetId == null) {
            cancelUpdate(null)
            return
        }

        val appWidgetData = getWidgetData(appWidgetId)
        if (appWidgetData == null) {
            cancelUpdate(null)
            return
        }

        setContent {
            MobileWeatherAppTheme(
                darkTheme = when (settings.nightMode) {
                    NightMode.Light -> false
                    NightMode.Dark -> true
                    else -> isSystemInDarkTheme()
                },
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) { innerPadding ->
                    CompositionLocalProvider(LocalInsetsPaddings provides innerPadding) {
                        ChangeLocation(widgetData = appWidgetData, onUpdate = {
                            updateWidgetData(widgetData = appWidgetData.copy(location = it))
                            approveUpdate(appWidgetId)
                        })
                    }
                }
            }
        }
    }

    fun updateWidgetData(widgetData: WeatherWidgetStorage.WeatherWidget) {
        WeatherWidgetStorage.updateOne(context = this, widget = widgetData)
    }

    fun approveUpdate(widgetId: Int) {
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, resultValue)

        val appWidgetManager = AppWidgetManager.getInstance(this)
        updateWeatherWidgets(context = this, appWidgetManager = appWidgetManager)

        finish()
    }

    fun cancelUpdate(widgetId: Int?) {
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, resultValue)
        finish()
    }

    fun getWidgetId(): Int? {
        val id = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        return if (id != AppWidgetManager.INVALID_APPWIDGET_ID) id else null
    }

    fun getWidgetData(widgetId: Int): WeatherWidgetStorage.WeatherWidget? {
        return WeatherWidgetStorage.getAll(this).find { it.id == widgetId }
    }

    @Composable
    fun ChangeLocation(
        modifier: Modifier = Modifier,
        widgetData: WeatherWidgetStorage.WeatherWidget,
        onUpdate: (String) -> Unit
    ) {
        val insets = LocalInsetsPaddings.current

        var selectedForecast by remember {
            mutableStateOf(settings.weatherForecasts.find { it.location.address == widgetData.location })
        }

        Column(
            modifier = modifier.padding(insets),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(R.string.saved_locations),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(.8f)
            )
            VerticalSpacer(value = 10.dp)

            val scroll = rememberLazyListState()

            LazyColumn(
                state = scroll,
                modifier = Modifier
                    .weight(1f)
                    .fadingEdges(scrollableState = scroll, isVertical = true),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(settings.weatherForecasts.toList()) { forecast ->
                    val isSelected = selectedForecast?.location == forecast.location

                    LocationCard(
                        modifier = Modifier.animateItem(),
                        location = forecast.location,
                        isSelected = isSelected,
                        onClick = {
                            selectedForecast = forecast
                        }
                    )
                }
            }
            VerticalSpacer(value = 20.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CustomFAB(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedForecast?.location?.address?.let { location ->
                            onUpdate(location)
                        }
                    },
                    icon = Icons.Rounded.Flag,
                    text = stringResource(R.string.update),
                )
            }
            VerticalSpacer(value = 40.dp)
        }
    }
}