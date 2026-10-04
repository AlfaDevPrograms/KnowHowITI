package com.khsuiti.knowhow.presentation.feature.tutorservices

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.EducationApi
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.SubjectsApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.responsesData.PageResponse
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
	}

	fun clearState() {
		_state.update { TutorServicesState() }
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
				val subjectsPage = runCatching { SubjectsApi.list(startIndex = 0, size = 50) }.getOrNull()

				val activeSet = dataStore.data.first()[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()
				val servicesWithActive = allFetchedServices.map { s ->
					if (activeSet.contains(s.idService)) s.copy(isActive = true) else s
				}

				_state.update {
					it.copy(
						services = servicesWithActive,
						subjects = subjectsPage?.items ?: emptyList(),
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
				dataStore.edit { prefs ->
					val set = (prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()).toMutableSet()
					set.add(created.idService)
					prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] = set
				}
				loadData()
			} catch (e: ApiException) {
				Log.e("TutorServicesVM", "createService error: ${e.code} ${e.body}", e)
				_state.update { it.copy(error = e.errorMessage() ?: e.body) }
			} catch (e: Exception) {
				Log.e("TutorServicesVM", "createService exception: ${e.message}", e)
				_state.update { it.copy(error = e.message) }
			}
		}
	}

	fun updateService(idService: UUID, idSubject: UUID, lessonDuration: Int, price: Double, description: String) {
		viewModelScope.launch {
			try {
				ServicesApi.putService(
					id = idService,
					idSubject = idSubject,
					lessonDurationMinutes = lessonDuration,
					priceInDollars = price,
					isOnlineFormat = true,
					description = description
				)
				dataStore.edit { prefs ->
					val set = (prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()).toMutableSet()
					set.add(idService.toString())
					prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] = set
				}
				loadData()
			} catch (e: ApiException) {
				Log.e("TutorServicesVM", "updateService error: ${e.code} ${e.body}", e)
				_state.update { it.copy(error = e.errorMessage() ?: e.body) }
			} catch (e: Exception) {
				Log.e("TutorServicesVM", "updateService exception: ${e.message}", e)
				_state.update { it.copy(error = e.message) }
			}
		}
	}

	fun toggleActive(idService: UUID, currentActive: Boolean) {
		val newActiveState = !currentActive
		val serviceIdStr = idService.toString()

		viewModelScope.launch {
			dataStore.edit { prefs ->
				val set = (prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] ?: emptySet()).toMutableSet()
				if (newActiveState) {
					set.add(serviceIdStr)
				} else {
					set.remove(serviceIdStr)
				}
				prefs[DataStoreKeys.ACTIVE_SERVICE_IDS] = set
			}
		}

		val updatedList = _state.value.services.map { s ->
			if (s.idService == serviceIdStr) s.copy(isActive = newActiveState) else s
		}
		_state.update { it.copy(services = updatedList) }

		viewModelScope.launch {
			try {
				ServicesApi.setActive(idService, newActiveState)
			} catch (e: Exception) {
				Log.e("TutorServicesVM", "toggleActive exception (ignored for UX): ${e.message}", e)
			}
		}
	}
}
