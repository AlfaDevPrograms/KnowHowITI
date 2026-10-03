package com.khsuiti.knowhow.presentation.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.EducationApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.data.local.UsersApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>
) : ViewModel() {

	private val _isTutor = MutableStateFlow(false)
	val isTutor: StateFlow<Boolean> = _isTutor.asStateFlow()

	private val _startDestination = MutableStateFlow<String?>(null)
	val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

	init {
		viewModelScope.launch {
			val prefs = dataStore.data.first()
			val token = prefs[DataStoreKeys.ACCESS_TOKEN]
			val refresh = prefs[DataStoreKeys.REFRESH_TOKEN]
			val savedIsTutor = prefs[DataStoreKeys.IS_TUTOR] ?: false

			if (!token.isNullOrBlank()) {
				ApiClient.setTokens(token, refresh)

				_isTutor.value = savedIsTutor
				_startDestination.value = if (savedIsTutor) BottomNavItem.TutorServices.route else BottomNavItem.Home.route

				launch(Dispatchers.IO) {
					val account = runCatching { UsersApi.getMyAccount() }.getOrNull()
					val roleName = account?.roleName.orEmpty()
					val isRoleTutor = roleName.contains("репетитор", ignoreCase = true) || roleName.contains("tutor", ignoreCase = true)

					val tutorProfileSuccess = try {
						UserProfilesApi.getMyTutorProfile()
						true
					} catch (e: Exception) {
						if ((e as? ApiException)?.errorCode() == "TutorProfileNotFound" && savedIsTutor) {
							val eduPage = runCatching { EducationApi.list(startIndex = 0, size = 1) }.getOrNull()
							val eduId = eduPage?.items?.firstOrNull()?.idEducation?.let { UUID.fromString(it) } ?: UUID.randomUUID()
							runCatching { UserProfilesApi.postCreateTutorProfile(eduId, 2, "Профессиональный репетитор") }
							true
						} else {
							false
						}
					}

					val actualIsTutor = savedIsTutor || isRoleTutor || tutorProfileSuccess
					if (actualIsTutor != savedIsTutor) {
						dataStore.edit { p -> p[DataStoreKeys.IS_TUTOR] = actualIsTutor }
					}
					_isTutor.value = actualIsTutor
				}
			} else {
				_isTutor.value = false
				_startDestination.value = "auth"
			}
		}
	}
}
