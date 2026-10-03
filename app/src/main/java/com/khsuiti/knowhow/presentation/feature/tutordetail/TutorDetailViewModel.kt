package com.khsuiti.knowhow.presentation.feature.tutordetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.BookingsApi
import com.khsuiti.knowhow.data.local.ReviewsApi
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TutorDetailViewModel @Inject constructor() : ViewModel() {

	private val _state = MutableStateFlow(TutorDetailState())
	val state: StateFlow<TutorDetailState> = _state.asStateFlow()

	fun handleIntent(intent: TutorDetailIntent) {
		when (intent) {
			is TutorDetailIntent.LoadTutorDetail -> loadTutorDetailData(intent.tutorId)
			is TutorDetailIntent.SelectDate -> _state.update { it.copy(selectedDay = intent.date) }
			is TutorDetailIntent.SelectTimeSlot -> _state.update { it.copy(selectedTimeSlot = intent.timeSlot) }
			is TutorDetailIntent.ConfirmBooking -> confirmBookingData()
			is TutorDetailIntent.CancelBooking -> _state.update { it.copy(bookingSuccess = false) }
		}
	}

	private fun loadTutorDetailData(tutorId: String) {
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val uuid = runCatching { UUID.fromString(tutorId) }.getOrNull()
				if (uuid != null) {
					val tutorDto = UserProfilesApi.getSelectTutorProfile(uuid)
					val reviewsPage = ReviewsApi.getTutorReviews(uuid, startIndex = 0, size = 10)
					val servicesPage = ServicesApi.getServices(tutorId = uuid, startIndex = 0, size = 10)

					_state.update {
						it.copy(
							tutor = tutorDto,
							service = servicesPage.items.firstOrNull(),
							reviews = reviewsPage.items,
							isLoading = false
						)
					}
				} else {
					_state.update { it.copy(isLoading = false, error = "Некорректный ID репетитора") }
				}
			} catch (e: ApiException) {
				Log.e("TutorDetailViewModel", "API Exception: ${e.code} ${e.errorMessage()}", e)
				_state.update {
					it.copy(
						isLoading = false,
						tutor = null,
						error = e.errorMessage() ?: "Ошибка загрузки профиля (${e.code})"
					)
				}
			} catch (e: Exception) {
				Log.e("TutorDetailViewModel", "Exception: ${e.message}", e)
				_state.update {
					it.copy(
						isLoading = false,
						tutor = null,
						error = "Нет соединения с сервером"
					)
				}
			}
		}
	}

	private fun confirmBookingData() {
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val scheduleId = _state.value.service?.idService?.let { runCatching { UUID.fromString(it) }.getOrNull() }
				if (scheduleId != null) {
					BookingsApi.postBooking(scheduleId)
					_state.update { it.copy(isLoading = false, bookingSuccess = true) }
				} else {
					_state.update { it.copy(isLoading = false, error = "Выберите доступное время занятий") }
				}
			} catch (e: ApiException) {
				Log.e("TutorDetailViewModel", "Booking API Exception: ${e.code} ${e.errorMessage()}", e)
				_state.update {
					it.copy(
						isLoading = false,
						error = e.errorMessage() ?: "Ошибка бронирования (${e.code})"
					)
				}
			} catch (e: Exception) {
				Log.e("TutorDetailViewModel", "Booking Exception: ${e.message}", e)
				_state.update {
					it.copy(
						isLoading = false,
						error = "Нет соединения с сервером"
					)
				}
			}
		}
	}
}
