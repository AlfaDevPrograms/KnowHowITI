package com.khsuiti.knowhow.presentation.feature.tutorschedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.BookingsApi
import com.khsuiti.knowhow.data.local.SchedulesApi
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.responsesData.BookingResponse
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
	val bookings: List<BookingResponse> = emptyList(),
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
				val tutorBookingsPage = runCatching { BookingsApi.getMyTutorBookings(startIndex = 0, size = 50).items }.getOrDefault(emptyList())

				val schedulePage = try {
					SchedulesApi.getMySchedule(startIndex = 0, size = 50)
				} catch (e: ApiException) {
					PageResponse(0, 0, 0, emptyList())
				}

				val servicesPage = try {
					ServicesApi.getMyServices(startIndex = 0, size = 50)
				} catch (e: Exception) {
					PageResponse(0, 0, 0, emptyList())
				}

				_state.update {
					it.copy(
						schedules = schedulePage.items,
						bookings = tutorBookingsPage,
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
				_state.update { it.copy(error = e.errorMessage() ?: e.message) }
			} catch (e: Exception) {
				_state.update { it.copy(error = e.message) }
			}
		}
	}

	fun updateScheduleSlot(idSchedule: UUID, idService: UUID, startTimeIso: String) {
		viewModelScope.launch {
			try {
				SchedulesApi.putSchedule(idSchedule, idService, startTimeIso)
				loadSchedule()
			} catch (e: ApiException) {
				_state.update { it.copy(error = e.errorMessage() ?: e.message) }
			} catch (e: Exception) {
				_state.update { it.copy(error = e.message) }
			}
		}
	}

	fun deleteScheduleSlot(idSchedule: UUID) {
		viewModelScope.launch {
			try {
				SchedulesApi.deleteSchedule(idSchedule)
				loadSchedule()
			} catch (e: Exception) {
				_state.update { it.copy(error = e.message) }
			}
		}
	}

	fun closeBooking(idBooking: UUID) {
		viewModelScope.launch {
			try {
				BookingsApi.closeBooking(idBooking)
				loadSchedule()
			} catch (e: Exception) {
				_state.update { it.copy(error = e.message) }
			}
		}
	}

	fun deleteBooking(idBooking: UUID) {
		viewModelScope.launch {
			try {
				BookingsApi.deleteBooking(idBooking)
				loadSchedule()
			} catch (e: Exception) {
				_state.update { it.copy(error = e.message) }
			}
		}
	}
}
