package com.khsuiti.knowhow.presentation.feature.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.AdminApi
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.DataStoreKeys.THEME_MODE_KEY
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.data.repository.PicturesRepository
import com.khsuiti.knowhow.responsesData.ServiceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	picturesRepository: PicturesRepository,
	@param:ApplicationContext private val context: Context
) : ViewModel() {

	val backgroundImageUri = picturesRepository.backgroundImageUri
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

	private val _state = MutableStateFlow(HomeState())
	val state: StateFlow<HomeState> = _state.asStateFlow()

	private val _hasShownSplash = mutableStateOf(false)
	val hasShownSplash: State<Boolean> = _hasShownSplash

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
		loadTutorsData()
	}

	fun clearState() {
		_state.update { HomeState() }
	}

	fun markSplashAsShown() {
		_hasShownSplash.value = true
	}

	fun handleIntent(intent: HomeIntent) {
		when (intent) {
			is HomeIntent.LoadData -> {
				loadTutorsData()
			}
			is HomeIntent.SearchQueryChanged -> {
				_state.update { it.copy(searchQuery = intent.query) }
			}
			is HomeIntent.SelectSubjectFilter -> {
				_state.update {
					it.copy(
						selectedSubject = if (intent.subject == "Все эксперты") null else intent.subject
					)
				}
			}
		}
	}

	private fun loadTutorsData() {
		if (ApiClient.accessToken.isNullOrBlank()) return
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val userTutorsPage = runCatching { UserProfilesApi.getAllTutorProfiles(startIndex = 0, size = 50).items }.getOrDefault(emptyList())
				val adminTutorsPage = runCatching { AdminApi.getTutorProfiles(startIndex = 0, size = 50).items }.getOrDefault(emptyList())

				val servicePage = runCatching { ServicesApi.getServices(startIndex = 0, size = 1000).items }.getOrDefault(emptyList())
				val tutorsFromServices = servicePage.map { it.tutor }

				val allTutors = (userTutorsPage + adminTutorsPage + tutorsFromServices).distinctBy { it.idTutorProfile }

				val allServices = ArrayList<ServiceResponse>()
				allServices.addAll(servicePage)

				for (tutor in allTutors) {
					val uuid = runCatching { UUID.fromString(tutor.idTutorProfile) }.getOrNull()
					if (uuid != null) {
						val tutorServices = runCatching { ServicesApi.getServices(tutorId = uuid, startIndex = 0, size = 1000).items }.getOrDefault(emptyList())
						allServices.addAll(tutorServices)
					}
				}

				val distinctServices = allServices.distinctBy { it.idService }

				_state.update {
					it.copy(
						tutors = allTutors,
						services = distinctServices,
						isLoading = false
					)
				}
			} catch (e: ApiException) {
				Log.e("HomeViewModel", "API Exception: ${e.code} ${e.errorMessage()}", e)
				if (e.code == 401) {
					ApiClient.clearTokens()
					dataStore.edit { prefs ->
						prefs.remove(DataStoreKeys.ACCESS_TOKEN)
						prefs.remove(DataStoreKeys.REFRESH_TOKEN)
						prefs.remove(DataStoreKeys.IS_TUTOR)
					}
				}
				_state.update {
					it.copy(
						isLoading = false,
						tutors = emptyList(),
						services = emptyList(),
						error = if (e.code == 401) "Требуется авторизация" else (e.errorMessage() ?: "Ошибка загрузки (${e.code})")
					)
				}
			} catch (e: Exception) {
				Log.e("HomeViewModel", "Exception: ${e.message}", e)
				_state.update {
					it.copy(
						isLoading = false,
						tutors = emptyList(),
						services = emptyList(),
						error = "Нет соединения с сервером"
					)
				}
			}
		}
	}
}
