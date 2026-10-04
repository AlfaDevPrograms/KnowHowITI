package com.khsuiti.knowhow.presentation.feature.tutorservices

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.EducationApi
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.SubjectsApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.responsesData.ServiceResponse
import com.khsuiti.knowhow.responsesData.SubjectResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TutorServicesState(
	val services: List<ServiceResponse> = emptyList(),
	val subjects: List<SubjectResponse> = emptyList(),
	val isLoading: Boolean = false,
	val error: String? = null
)

@HiltViewModel
class TutorServicesViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>
) : ViewModel() {

	private val _state = MutableStateFlow(TutorServicesState())
	val state: StateFlow<TutorServicesState> = _state.asStateFlow()

	init {
		loadData()
		loadSubjects()
	}

	fun clearState() {
		_state.update { TutorServicesState() }
	}

	fun loadSubjects() {
		viewModelScope.launch {
			try {
				val page = SubjectsApi.list(startIndex = 0, size = 100)
				_state.update { it.copy(subjects = page.items) }
			} catch (e: Exception) {
				Log.e("TutorServicesVM", "Error loading subjects from /Subjects: ${e.message}", e)
			}
		}
	}

	fun loadData() {
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val myServices = runCatching { ServicesApi.getMyServices(startIndex = 0, size = 50).items }.getOrDefault(emptyList())
				val tutorProfile = runCatching { UserProfilesApi.getMyTutorProfile() }.getOrNull()
				val tutorIdUuid = tutorProfile?.idTutorProfile?.let { runCatching { UUID.fromString(it) }.getOrNull() }
				val tutorServices = if (tutorIdUuid != null) {
					runCatching { ServicesApi.getServices(tutorId = tutorIdUuid, startIndex = 0, size = 50).items }.getOrDefault(emptyList())
				} else {
					emptyList()
				}

				val allFetchedServices = (myServices + tutorServices).distinctBy { it.idService }
				val subjectsPage = runCatching { SubjectsApi.list(startIndex = 0, size = 100) }.getOrNull()

				val activeSet = dataStore.data.first()[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()
				val servicesWithActive = allFetchedServices.map { s ->
					if (activeSet.contains(s.idService)) s.copy(isActive = true) else s
				}

				_state.update {
					it.copy(
						services = servicesWithActive,
						subjects = subjectsPage?.items.orEmpty().ifEmpty { it.subjects },
						isLoading = false,
						error = null
					)
				}
			} catch (e: Exception) {
				Log.e("TutorServicesVM", "Error loading services: ${e.message}", e)
				_state.update { it.copy(isLoading = false) }
			}
		}
	}

	fun createService(idSubject: UUID, lessonDuration: Int, price: Double, description: String) {
		viewModelScope.launch {
			try {
				val created = ServicesApi.postService(
					idSubject = idSubject,
					lessonDurationMinutes = lessonDuration,
					priceInDollars = price,
					isOnlineFormat = true,
					description = description
				)
				
				val serviceUuid = runCatching { UUID.fromString(created.idService) }.getOrNull()
				val activeCreated = if (serviceUuid != null) {
					runCatching { ServicesApi.setActive(serviceUuid, true) }.getOrDefault(created.copy(isActive = true))
				} else {
					created.copy(isActive = true)
				}

				dataStore.edit { prefs ->
					val set = (prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()).toMutableSet()
					set.add(activeCreated.idService)
					prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] = set
				}

				_state.update { it.copy(services = it.services + activeCreated) }
				loadData()
			} catch (e: Exception) {
				Log.e("TutorServicesVM", "createService exception: ${e.message}", e)
				_state.update { it.copy(error = e.message ?: "Ошибка создания услуги") }
			}
		}
	}

	fun updateService(idService: UUID, idSubject: UUID, lessonDuration: Int, price: Double, description: String) {
		viewModelScope.launch {
			try {
				val updated = ServicesApi.putService(
					id = idService,
					idSubject = idSubject,
					lessonDurationMinutes = lessonDuration,
					priceInDollars = price,
					isOnlineFormat = true,
					description = description
				)
				runCatching { ServicesApi.setActive(idService, true) }
				dataStore.edit { prefs ->
					val set = (prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()).toMutableSet()
					set.add(idService.toString())
					prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] = set
				}
				_state.update { st ->
					st.copy(services = st.services.map { if (it.idService == idService.toString()) updated.copy(isActive = true) else it })
				}
				loadData()
			} catch (e: Exception) {
				Log.e("TutorServicesVM", "updateService exception: ${e.message}", e)
				_state.update { it.copy(error = e.message ?: "Ошибка обновления услуги") }
			}
		}
	}

	fun toggleActive(idServiceStr: String, currentActive: Boolean) {
		val newActiveState = !currentActive

		// Optimistic UI update immediately
		val updatedList = _state.value.services.map { s ->
			if (s.idService == idServiceStr) s.copy(isActive = newActiveState) else s
		}
		_state.update { it.copy(services = updatedList) }

		viewModelScope.launch {
			dataStore.edit { prefs ->
				val set = (prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()).toMutableSet()
				if (newActiveState) {
					set.add(idServiceStr)
				} else {
					set.remove(idServiceStr)
				}
				prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] = set
			}

			val serviceUuid = runCatching { UUID.fromString(idServiceStr) }.getOrNull()
			if (serviceUuid != null) {
				runCatching {
					val updatedFromApi = ServicesApi.setActive(serviceUuid, newActiveState)
					_state.update { st ->
						st.copy(services = st.services.map { if (it.idService == idServiceStr) updatedFromApi else it })
					}
				}
			}
		}
	}
}
