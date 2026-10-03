package com.khsuiti.knowhow.presentation.feature.profile

sealed interface ProfileIntent {
	data object LoadProfile : ProfileIntent
	data class ToggleNotifications(val enabled: Boolean) : ProfileIntent
	data object Logout : ProfileIntent
}
