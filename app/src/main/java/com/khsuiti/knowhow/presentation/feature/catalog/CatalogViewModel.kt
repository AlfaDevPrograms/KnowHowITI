package com.khsuiti.knowhow.presentation.feature.catalog

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.DataStoreKeys.THEME_MODE_KEY
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.SubjectsApi
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.responsesData.ServiceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>,
) : ViewModel() {

	private val _state = MutableStateFlow(CatalogState())
	val state: StateFlow<CatalogState> = _state.asStateFlow()

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
		loadSubjectsData()
	}

	fun clearState() {
		_state.update { CatalogState() }
	}

	fun handleIntent(intent: CatalogIntent) {
		when (intent) {
			is CatalogIntent.LoadCatalog -> loadSubjectsData()
			is CatalogIntent.SearchQueryChanged -> {
				_state.update { it.copy(searchQuery = intent.query) }
			}
			is CatalogIntent.SelectCategoryFilter -> {
				_state.update {
					it.copy(selectedCategory = if (intent.category == "Все предметы") null else intent.category)
				}
			}
			is CatalogIntent.SelectSubject -> {
				// Handled by navigation in UI
			}
		}
	}

	private fun loadSubjectsData() {
		if (ApiClient.accessToken.isNullOrBlank()) return
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val apiSubjectsPage = runCatching { SubjectsApi.list(startIndex = 0, size = 100) }.getOrNull()
				val categories = if (apiSubjectsPage != null && apiSubjectsPage.items.isNotEmpty()) {
					listOf("Все предметы") + apiSubjectsPage.items.map { it.name }.distinct()
				} else {
					_state.value.popularCategories
				}

				val allServices = ArrayList<ServiceResponse>()
				val publicServices = runCatching { ServicesApi.getServices(startIndex = 0, size = 1000).items }.getOrDefault(emptyList())
				allServices.addAll(publicServices)

				val allTutors = runCatching { UserProfilesApi.getAllTutorProfiles(startIndex = 0, size = 100).items }.getOrDefault(emptyList())
				for (tutor in allTutors) {
					val uuid = runCatching { UUID.fromString(tutor.idTutorProfile) }.getOrNull()
					if (uuid != null) {
						val tutorServices = runCatching { ServicesApi.getServices(tutorId = uuid, startIndex = 0, size = 1000).items }.getOrDefault(emptyList())
						allServices.addAll(tutorServices)
					}
				}

				val distinctServices = allServices.distinctBy { it.idService }

				val mappedItems = distinctServices.map { service ->
					SubjectCatalogItem(
						service = service,
						subjectName = service.subjectName,
						description = service.description ?: "Индивидуальные занятия: ${service.subjectName} (${service.lessonDurationMinutes} мин, \$${service.priceInDollars}/час)",
						rating = service.tutor.averageRating ?: 4.9,
						reviewCount = service.tutor.reviewCount,
						subtopics = service.description?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: listOf(service.subjectName),
						tutorCount = 1,
						gradeLevel = "5-11 класс",
						photoUrl = service.tutor.photoURL ?: "https://images.unsplash.com/photo-1635070041078-e363dbe005cb"
					)
				}

				_state.update {
					it.copy(
						subjects = mappedItems,
						popularCategories = categories,
						isLoading = false
					)
				}
			} catch (e: ApiException) {
				Log.e("CatalogViewModel", "API Exception: ${e.code} ${e.errorMessage()}", e)
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
						subjects = emptyList(),
						error = if (e.code == 401) "Требуется авторизация" else (e.errorMessage() ?: "Ошибка загрузки каталога (${e.code})")
					)
				}
			} catch (e: Exception) {
				Log.e("CatalogViewModel", "Exception: ${e.message}", e)
				_state.update {
					it.copy(
						isLoading = false,
						subjects = emptyList(),
						error = "Ошибка загрузки каталога"
					)
				}
			}
		}
	}
}
