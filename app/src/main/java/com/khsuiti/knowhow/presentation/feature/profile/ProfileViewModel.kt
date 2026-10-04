package com.khsuiti.knowhow.presentation.feature.profile

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.AuthApi
import com.khsuiti.knowhow.data.local.BookingsApi
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.DataStoreKeys.THEME_MODE_KEY
import com.khsuiti.knowhow.data.local.EducationApi
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.data.local.UsersApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>,
) : ViewModel() {

	private val _state = MutableStateFlow(ProfileState())
	val state: StateFlow<ProfileState> = _state.asStateFlow()

	private val _logoutSuccess = MutableStateFlow(false)
	val logoutSuccess: StateFlow<Boolean> = _logoutSuccess.asStateFlow()

	val themeMode: StateFlow<ThemeMode> = dataStore.data
		.catch { e ->
			if (e is IOException) emit(emptyPreferences()) else throw e
		}
		.map { prefs ->
			val raw = prefs[THEME_MODE_KEY] ?: ThemeMode.System.name
			runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.System)
		}
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = ThemeMode.System,
		)

	init {
		loadProfileData()
	}

	fun handleIntent(intent: ProfileIntent) {
		when (intent) {
			is ProfileIntent.LoadProfile -> loadProfileData()
			is ProfileIntent.ToggleNotifications ->
				_state.update { it.copy(notificationsEnabled = intent.enabled) }
			is ProfileIntent.Logout -> performLogout()
		}
	}

	fun resetLogoutState() {
		_logoutSuccess.update { false }
	}

	fun clearState() {
		_state.update { ProfileState() }
		_logoutSuccess.update { false }
	}

	private fun loadProfileData() {
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				if (ApiClient.accessToken.isNullOrBlank()) {
					val prefs = dataStore.data.first()
					val token = prefs[DataStoreKeys.ACCESS_TOKEN]
					val refresh = prefs[DataStoreKeys.REFRESH_TOKEN]
					if (!token.isNullOrBlank()) {
						ApiClient.setTokens(token, refresh)
					}
				}

				val accountInfo = UsersApi.getMyAccount()
				val role = accountInfo.roleName.lowercase()
				val isTutor = dataStore.data.first()[DataStoreKeys.IS_TUTOR] ?: (role.contains("tutor") || role.contains("репетитор") || role.contains("преподаватель"))

				dataStore.edit { prefs ->
					prefs[DataStoreKeys.IS_TUTOR] = isTutor
				}

				var completed = 0
				var balance = 0
				var courses = 0

				if (isTutor) {
					try {
						val tutorBookings = BookingsApi.getMyTutorBookings(startIndex = 0, size = 50)
						completed = tutorBookings.items.count { b -> b.isClose }
						balance = tutorBookings.items.count { b -> !b.isClose }
						courses = tutorBookings.items.mapNotNull { b -> b.service?.idSubject }.distinct().size
					} catch (e: Exception) {
						if ((e as? ApiException)?.errorCode() == "TutorProfileNotFound") {
							val eduPage = runCatching { EducationApi.list(startIndex = 0, size = 1) }.getOrNull()
							val eduId = eduPage?.items?.firstOrNull()?.idEducation?.let { UUID.fromString(it) } ?: UUID.randomUUID()
							runCatching { UserProfilesApi.postCreateTutorProfile(eduId, 2, "Профессиональный репетитор") }
						}
					}
				} else {
					try {
						val studentBookings = BookingsApi.getMyStudentBookings(startIndex = 0, size = 50)
						completed = studentBookings.items.count { b -> b.isClose }
						balance = studentBookings.items.count { b -> !b.isClose }
						courses = studentBookings.items.mapNotNull { b -> b.service?.idSubject }.distinct().size
					} catch (e: Exception) {
						if ((e as? ApiException)?.errorCode() == "StudentProfileNotFound") {
							runCatching { UserProfilesApi.postCreateStudentProfile() }
						}
					}
				}

				_state.update {
					it.copy(
						accountInfo = accountInfo,
						completedLessonsCount = completed,
						balanceLessonsCount = balance,
						activeCoursesCount = courses,
						isLoading = false
					)
				}
			} catch (e: ApiException) {
				Log.e("ProfileViewModel", "API Exception: ${e.code} ${e.errorMessage()}", e)
				_state.update {
					it.copy(
						isLoading = false,
						error = if (e.code == 401) "Требуется авторизация" else (e.errorMessage() ?: "Ошибка загрузки профиля (${e.code})")
					)
				}
			} catch (e: Exception) {
				Log.e("ProfileViewModel", "Exception: ${e.message}", e)
				_state.update {
					it.copy(
						isLoading = false,
						error = "Нет соединения с сервером"
					)
				}
			}
		}
	}

	private fun performLogout() {
		viewModelScope.launch {
			try {
				AuthApi.postLogout("android_device_id")
			} catch (e: Exception) {
				Log.e("ProfileViewModel", "Logout Exception: ${e.message}", e)
			} finally {
				dataStore.edit { prefs ->
					prefs.remove(DataStoreKeys.ACCESS_TOKEN)
					prefs.remove(DataStoreKeys.REFRESH_TOKEN)
					prefs.remove(DataStoreKeys.IS_TUTOR)
				}
				ApiClient.clearTokens()
				_state.update { ProfileState() }
				_logoutSuccess.update { true }
			}
		}
	}
}
