package com.example.imagesproject.data.datastore

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.imagesproject.core.util.isPowerSavingMode
import com.example.imagesproject.domain.datastore.UserPreferences
import com.example.imagesproject.domain.model.AppConfiguration
import com.example.imagesproject.domain.type.ThemeStyleType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


class UserPreferencesImplDataStore @Inject constructor(
    private val dataStorePreferences: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
) : UserPreferences {
    private val tag = this::class.java.simpleName
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = goAsync {

            if (intent.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                togglePowerSavingMode()
            }
        }
    }
    init {
        context.registerReceiver(broadcastReceiver, android.content.IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
    }

    override val appConfigurationStream: Flow<AppConfiguration> = dataStorePreferences.data
        .catch { exception ->
            exception.localizedMessage?.let { Log.e(tag, it) }
            emit(value = emptyPreferences())
        }
        .map { preferences ->
            val useDynamicColors = preferences[PreferencesKeys.useDynamicColors] ?: true
            val usePowerSavingMode = preferences[PreferencesKeys.usePowerSavingMode] ?: false
            val themeStyle = preferences[PreferencesKeys.themeStyle].toThemeStyleType()
            AppConfiguration(
                useDynamicColors = useDynamicColors,
                themeStyle = themeStyle,
                usePowerSavingMode = usePowerSavingMode,
            )
        }

    private fun BroadcastReceiver.goAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob()).launch(context) {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override suspend fun toggleDynamicColors() {
        tryIt {
            dataStorePreferences.edit { preferences ->
                val current = preferences[PreferencesKeys.useDynamicColors] ?: true
                preferences[PreferencesKeys.useDynamicColors] = !current
            }
        }
    }

    private suspend fun togglePowerSavingMode() {
        tryIt {
            dataStorePreferences.edit { preferences ->
                val current = preferences[PreferencesKeys.usePowerSavingMode] ?: isPowerSavingMode(context = context)
                preferences[PreferencesKeys.useDynamicColors] = !current
            }
        }
    }

    override suspend fun changeThemeStyle(themeStyle: ThemeStyleType) {
        tryIt {
            dataStorePreferences.updateData {
                val prefs = it.toPreferences().toMutablePreferences()
                prefs[PreferencesKeys.themeStyle] = themeStyle.name
                prefs
            }
        }
    }

    private suspend fun tryIt(action: suspend () -> Unit) {
        try {
            action()
        } catch (exception: Exception) {
            exception.localizedMessage?.let { Log.e(tag, it) }
        }
    }

    private fun String?.toThemeStyleType(): ThemeStyleType = when (this) {
        ThemeStyleType.LightMode.name -> ThemeStyleType.LightMode
        ThemeStyleType.DarkMode.name -> ThemeStyleType.DarkMode
        else -> ThemeStyleType.FollowAndroidSystem
    }

    private object PreferencesKeys {
        val useDynamicColors = booleanPreferencesKey(name = "use_dynamic_colors")
        val usePowerSavingMode = booleanPreferencesKey(name = "use_power_saving_mode")
        val themeStyle = stringPreferencesKey(name = "theme_style")
    }
}