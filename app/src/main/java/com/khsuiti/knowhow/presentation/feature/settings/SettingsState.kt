package com.khsuiti.knowhow.presentation.feature.settings

import androidx.compose.runtime.Immutable
import com.khsuiti.knowhow.data.local.ThemeMode

@Immutable
data class SettingsState(
	val isLoading: Boolean = false,
	val error: String? = null,
	val themeMode: ThemeMode = ThemeMode.System,
	val notificationsEnabled: Boolean = true,
	val dimmingLevel: Float = 0.15f,
	val isBackgroundAnimationEnabled: Boolean = true
)

