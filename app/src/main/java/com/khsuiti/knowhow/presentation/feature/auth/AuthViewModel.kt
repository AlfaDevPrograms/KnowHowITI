package com.khsuiti.knowhow.presentation.feature.auth

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.AuthApi
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.EducationApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.data.local.UsersApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>
) : ViewModel() {

	private val _state = MutableStateFlow(AuthState())
	val state: StateFlow<AuthState> = _state.asStateFlow()

	fun handleIntent(intent: AuthIntent) {
		when (intent) {
			is AuthIntent.LoginQueryChanged -> _state.update { it.copy(loginOrEmail = intent.login, error = null) }
			is AuthIntent.EmailChanged -> _state.update { it.copy(email = intent.email, error = null) }
			is AuthIntent.PasswordChanged -> _state.update { it.copy(password = intent.pass, error = null) }
			is AuthIntent.FirstNameChanged -> _state.update { it.copy(firstName = intent.name, error = null) }
			is AuthIntent.LastNameChanged -> _state.update { it.copy(lastName = intent.name, error = null) }
			is AuthIntent.ToggleAuthMode -> _state.update { it.copy(isLoginMode = intent.isLoginMode, error = null) }
			is AuthIntent.ToggleAccountType -> _state.update { it.copy(isTutor = intent.isTutor) }
			is AuthIntent.BioChanged -> _state.update { it.copy(bio = intent.bio) }
			is AuthIntent.ExperienceChanged -> _state.update { it.copy(experienceYears = intent.exp) }
			is AuthIntent.ToggleRememberMe -> _state.update { it.copy(rememberMe = intent.remember) }
			is AuthIntent.SubmitAuth -> submitAuth()
			is AuthIntent.ResetAuth -> _state.update { AuthState() }
		}
	}

	private fun submitAuth() {
		val currentState = _state.value
		if (currentState.isLoginMode) {
			if (currentState.loginOrEmail.isBlank() || currentState.password.isBlank()) {
				_state.update { it.copy(error = "Заполните все поля") }
				return
			}
			_state.update { it.copy(isLoading = true, error = null) }
			viewModelScope.launch {
				try {
					val result = AuthApi.postAuth(
						loginOrEmail = currentState.loginOrEmail,
						password = currentState.password,
						idDevice = "android_device_id",
						isRememberThirtyDays = currentState.rememberMe
					)
					ApiClient.setTokens(result.accessToken, result.refreshToken)

					val isTutorProfile = runCatching { UserProfilesApi.getMyTutorProfile() }.isSuccess
					val accountInfo = runCatching { UsersApi.getMyAccount() }.getOrNull()
					val roleName = accountInfo?.roleName.orEmpty().lowercase()
					val isRoleTutor = roleName.contains("репетитор") || roleName.contains("tutor") || roleName.contains("преподаватель")

					val isTutor = isRoleTutor || isTutorProfile

					if (isTutor) {
						val tutorProfile = runCatching { UserProfilesApi.getMyTutorProfile() }.getOrNull()
						val eduPage = runCatching { EducationApi.list(startIndex = 0, size = 1) }.getOrNull()
						val eduId = eduPage?.items?.firstOrNull()?.idEducation?.let { UUID.fromString(it) } ?: UUID.randomUUID()
						if (tutorProfile != null) {
							runCatching {
								UserProfilesApi.putMyTutorProfile(eduId, tutorProfile.experienceYear, tutorProfile.bio)
							}
						} else {
							runCatching {
								UserProfilesApi.postCreateTutorProfile(eduId, 2, "Профессиональный репетитор")
							}
						}
					}

					dataStore.edit { prefs ->
						prefs[DataStoreKeys.ACCESS_TOKEN] = result.accessToken
						prefs[DataStoreKeys.REFRESH_TOKEN] = result.refreshToken
						prefs[DataStoreKeys.IS_TUTOR] = isTutor
					}
					_state.update { it.copy(isLoading = false, isSuccess = true) }
				} catch (e: Exception) {
					Log.e("AuthViewModel", "Auth error: ${e.message}", e)
					_state.update { it.copy(isLoading = false, error = e.message ?: "Ошибка авторизации") }
				}
			}
		} else {
			if (currentState.loginOrEmail.isBlank() || currentState.email.isBlank() || currentState.password.isBlank()) {
				_state.update { it.copy(error = "Заполните все обязательные поля") }
				return
			}
			_state.update { it.copy(isLoading = true, error = null) }
			viewModelScope.launch {
				try {
					AuthApi.postRegister(
						login = currentState.loginOrEmail,
						email = currentState.email,
						password = currentState.password,
						firstName = currentState.firstName.ifBlank { "Пользователь" },
						lastName = currentState.lastName.ifBlank { "Новый" }
					)
					val result = AuthApi.postAuth(
						loginOrEmail = currentState.loginOrEmail,
						password = currentState.password,
						idDevice = "android_device_id",
						isRememberThirtyDays = true
					)
					ApiClient.setTokens(result.accessToken, result.refreshToken)

					val isTutor = currentState.isTutor
					if (isTutor) {
						val eduPage = runCatching { EducationApi.list(startIndex = 0, size = 1) }.getOrNull()
						val eduId = eduPage?.items?.firstOrNull()?.idEducation?.let { UUID.fromString(it) } ?: UUID.randomUUID()
						runCatching {
							UserProfilesApi.postCreateTutorProfile(
								idEducation = eduId,
								experienceYear = currentState.experienceYears,
								bio = currentState.bio.ifBlank { "Профессиональный репетитор" }
							)
						}
					} else {
						runCatching {
							UserProfilesApi.postCreateStudentProfile()
						}
					}

					dataStore.edit { prefs ->
						prefs[DataStoreKeys.ACCESS_TOKEN] = result.accessToken
						prefs[DataStoreKeys.REFRESH_TOKEN] = result.refreshToken
						prefs[DataStoreKeys.IS_TUTOR] = isTutor
					}

					_state.update { it.copy(isLoading = false, isSuccess = true) }
				} catch (e: Exception) {
					Log.e("AuthViewModel", "Register error: ${e.message}", e)
					_state.update { it.copy(isLoading = false, error = e.message ?: "Ошибка регистрации") }
				}
			}
		}
	}
}
