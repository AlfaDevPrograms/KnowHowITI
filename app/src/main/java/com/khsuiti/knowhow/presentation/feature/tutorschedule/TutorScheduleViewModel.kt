package com.khsuiti.knowhow.presentation.feature.tutorschedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.EducationApi
import com.khsuiti.knowhow.data.local.SchedulesApi
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.responsesData.PageResponse
import com.khsuiti.knowhow.responsesData.ScheduleResponse
import com.khsuiti.knowhow.responsesData.ServiceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TutorScheduleState(
	val schedules: List<ScheduleResponse> = emptyList(),
	val services: List<ServiceResponse> = emptyList(),
	val isLoading: Boolean = false,
	val error: String? = null
)

@HiltViewModel
class TutorScheduleViewModel @Inject constructor() : ViewModel() {

	private val _state = MutableStateFlow(TutorScheduleState())
	val state: StateFlow<TutorScheduleState> = _state.asStateFlow()

	init {
		loadSchedule()
	}

	fun clearState() {
		_state.update { TutorScheduleState() }
	}

	fun loadSchedule() {
		if (ApiClient.accessToken.isNullOrBlank()) return
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val schedulePage = try {
					SchedulesApi.getMySchedule(startIndex = 0, size = 50)
				} catch (e: ApiException) {
					if (e.errorCode() == "TutorProfileNotFound") {
						val eduPage = runCatching { EducationApi.list(startIndex = 0, size = 1) }.getOrNull()
						val eduId = eduPage?.items?.firstOrNull()?.idEducation?.let { UUID.fromString(it) } ?: UUID.randomUUID()
						runCatching { UserProfilesApi.postCreateTutorProfile(eduId, 2, "Профессиональный репетитор") }
						runCatching { SchedulesApi.getMySchedule(startIndex = 0, size = 50) }.getOrElse { PageResponse(0, 0, 0, emptyList()) }
					} else {
						PageResponse(0, 0, 0, emptyList())
					}
				}

				val servicesPage = try {
					ServicesApi.getMyServices(startIndex = 0, size = 50)
				} catch (e: Exception) {
					PageResponse(0, 0, 0, emptyList())
				}

				_state.update {
					it.copy(
						schedules = schedulePage.items,
						services = servicesPage.items,
						isLoading = false,
						error = null
					)
				}
			} catch (e: Exception) {
				Log.e("TutorScheduleVM", "Error loading schedule: ${e.message}", e)
				_state.update { it.copy(isLoading = false, error = null) }
			}
		}
	}

	fun addScheduleSlot(idService: UUID, startTimeIso: String) {
		viewModelScope.launch {
			try {
				SchedulesApi.postSchedule(idService, startTimeIso)
				loadSchedule()
			} catch (e: ApiException) {
				val msg = if (e.errorCode() == "TutorNotVerified" || e.code == 403) {
					"Профиль репетитора ожидает подтверждения администратором"
				} else {
					e.errorMessage() ?: e.message
				}
				_state.update { it.copy(error = msg) }
			} catch (e: Exception) {
				_state.update { it.copy(error = e.message) }
			}
		}
	}
}
