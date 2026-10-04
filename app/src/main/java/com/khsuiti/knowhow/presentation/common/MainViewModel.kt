package com.khsuiti.knowhow.presentation.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.DataStoreKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppAuthState(
	val isAuthenticated: Boolean = false,
	val isTutor: Boolean = false,
	val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
	dataStore: DataStore<Preferences>
) : ViewModel() {

	val appAuthState: StateFlow<AppAuthState> = dataStore.data
		.map { prefs ->
			val token = prefs[DataStoreKeys.ACCESS_TOKEN]
			val refresh = prefs[DataStoreKeys.REFRESH_TOKEN]
			val isTutor = prefs[DataStoreKeys.IS_TUTOR] ?: false
			if (!token.isNullOrBlank()) {
				ApiClient.setTokens(token, refresh)
				AppAuthState(isAuthenticated = true, isTutor = isTutor, isLoading = false)
			} else {
				ApiClient.clearTokens()
				AppAuthState(isAuthenticated = false, isTutor = false, isLoading = false)
			}
		}
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.Eagerly,
			initialValue = AppAuthState(isLoading = true)
		)
}
