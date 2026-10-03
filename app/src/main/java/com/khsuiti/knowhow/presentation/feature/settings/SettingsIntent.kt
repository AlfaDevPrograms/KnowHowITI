package com.khsuiti.knowhow.presentation.feature.settings

import com.khsuiti.knowhow.data.local.ThemeMode

sealed interface SettingsIntent {
	object LoadData : SettingsIntent
	data class SetThemeMode(val mode: ThemeMode) : SettingsIntent
	data class UpdateDimmingLevel(val level: Float) : SettingsIntent
	data class ToggleBackgroundAnimation(val enabled: Boolean) : SettingsIntent
}