package com.khsuiti.knowhow.presentation.feature.settings

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.data.repository.PicturesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException


@HiltViewModel
class SettingsViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	private val picturesRepository: PicturesRepository,
) : ViewModel() {
	
	companion object {
		val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
		val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
		private val DIMMING_LEVEL_KEY = stringPreferencesKey("dimming_level")
		private val BACKGROUND_ANIMATION_ENABLED_KEY =
			booleanPreferencesKey("background_animation_enabled")
	}
	
	private val _state = MutableStateFlow(SettingsState())
	val state: StateFlow<SettingsState> = _state.asStateFlow()
	
	init {
		loadSettings()
	}
	
	fun saveBackgroundImage(uri: Uri) = viewModelScope.launch {
		picturesRepository.saveBackgroungImageUri(uri)
	}
	
	fun deleteBackgroundImage() = viewModelScope.launch {
		picturesRepository.deleteBackgroundImage()
	}
	
	fun handleIntent(intent: SettingsIntent) {
		when (intent) {
			is SettingsIntent.LoadData -> loadSettings()
			is SettingsIntent.SetThemeMode -> setThemeMode(intent.mode)
			is SettingsIntent.UpdateDimmingLevel -> updateDimmingLevel(intent.level)
			is SettingsIntent.ToggleBackgroundAnimation -> toggleBackgroundAnimation(intent.enabled)
		}
	}
	
	private fun loadSettings() {
		viewModelScope.launch {
			_state.value = _state.value.copy(isLoading = true)
			
			val prefs = dataStore.data
				.catch { exception ->
					if (exception is IOException) {
						emit(emptyPreferences())
					} else {
						throw exception
					}
				}
				.first()
			val backgroundAnimEnabled = prefs[BACKGROUND_ANIMATION_ENABLED_KEY] ?: true
			val notifications = prefs[NOTIFICATIONS_ENABLED_KEY] ?: true
			val themeModeString = prefs[THEME_MODE_KEY] ?: ThemeMode.System.name
			val dimmingStr = prefs[DIMMING_LEVEL_KEY] ?: "0.4"
			
			val themeMode = try {
				ThemeMode.valueOf(themeModeString)
			} catch (_: IllegalArgumentException) {
				ThemeMode.Light
			}
			
			val dimmingLevel = try {
				dimmingStr.toFloat().coerceIn(0f, 1f)
			} catch (_: NumberFormatException) {
				0.4f
			}
			
			
			_state.value = _state.value.copy(
				isBackgroundAnimationEnabled = backgroundAnimEnabled,
				isLoading = false,
				notificationsEnabled = notifications,
				themeMode = themeMode,
				dimmingLevel = dimmingLevel,
			)
		}
	}
	
	private fun toggleBackgroundAnimation(enabled: Boolean) {
		_state.value = _state.value.copy(isBackgroundAnimationEnabled = enabled)
		viewModelScope.launch {
			dataStore.edit { preferences ->
				preferences[BACKGROUND_ANIMATION_ENABLED_KEY] = enabled
			}
		}
	}
	
	private fun setThemeMode(mode: ThemeMode) {
		_state.value = _state.value.copy(themeMode = mode)
		viewModelScope.launch {
			dataStore.edit { preferences ->
				preferences[THEME_MODE_KEY] = mode.name
			}
		}
	}
	
	private fun updateDimmingLevel(level: Float) {
		val clamped = level.coerceIn(0f, 1f)
		_state.value = _state.value.copy(dimmingLevel = clamped)
		viewModelScope.launch {
			dataStore.edit { preferences ->
				preferences[DIMMING_LEVEL_KEY] = clamped.toString()
			}
		}
	}
}