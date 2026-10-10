package com.khsuiti.knowhow.presentation.feature.tutordetail

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.ApiException
import com.khsuiti.knowhow.data.local.BookingsApi
import com.khsuiti.knowhow.data.local.DataStoreKeys
import com.khsuiti.knowhow.data.local.ReviewsApi
import com.khsuiti.knowhow.data.local.SchedulesApi
import com.khsuiti.knowhow.data.local.ServicesApi
import com.khsuiti.knowhow.data.local.UserProfilesApi
import com.khsuiti.knowhow.data.local.parseScheduleDateTime
import com.khsuiti.knowhow.responsesData.ScheduleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TutorDetailViewModel @Inject constructor(
	private val dataStore: DataStore<Preferences>
) : ViewModel() {

	private val _state = MutableStateFlow(TutorDetailState())
	val state: StateFlow<TutorDetailState> = _state.asStateFlow()

	fun handleIntent(intent: TutorDetailIntent) {
		when (intent) {
			is TutorDetailIntent.LoadTutorDetail -> loadTutorDetailData(intent.tutorId)
			is TutorDetailIntent.LoadServiceDetail -> loadServiceDetailData(intent.serviceId)
			is TutorDetailIntent.SelectService -> loadServiceSchedule(intent.serviceId)
			is TutorDetailIntent.SelectDate -> {
				val day = intent.date
				val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
				val firstScheduleForDay = _state.value.availableSchedules.firstOrNull { schedule ->
					parseScheduleDateTime(schedule.startTime)?.let { dt ->
						val dayFormat = DateTimeFormatter.ofPattern("E", Locale.forLanguageTag("ru"))
						val dayNumFormat = DateTimeFormatter.ofPattern("d")
						val dayName = dt.format(dayFormat).replaceFirstChar { it.uppercase() }
						val dayNum = dt.format(dayNumFormat)
						"$dayName $dayNum" == day
					} == true
				}
				_state.update {
					it.copy(
						selectedDay = day,
						selectedScheduleId = firstScheduleForDay?.idSchedule ?: it.selectedScheduleId,
						selectedTimeSlot = firstScheduleForDay?.let { s -> parseScheduleDateTime(s.startTime)?.format(timeFormat) } ?: it.selectedTimeSlot
					)
				}
			}
			is TutorDetailIntent.SelectTimeSlot -> _state.update { it.copy(selectedTimeSlot = intent.timeSlot) }
			is TutorDetailIntent.SelectScheduleSlot -> _state.update { it.copy(selectedScheduleId = intent.scheduleId, selectedTimeSlot = intent.timeText) }
			is TutorDetailIntent.ConfirmBooking -> confirmBookingData()
			is TutorDetailIntent.CancelBooking -> _state.update { it.copy(bookingSuccess = false) }
		}
	}

	private fun updateSchedulesAndDays(schedules: List<ScheduleResponse>, currentSelectedDay: String): Pair<List<Pair<String, String>>, String> {
		val dayFormat = DateTimeFormatter.ofPattern("E", Locale.forLanguageTag("ru"))
		val dayNumFormat = DateTimeFormatter.ofPattern("d")

		val daysMap = mutableListOf<Pair<String, String>>()
		schedules.forEach { s ->
			parseScheduleDateTime(s.startTime)?.let { dt ->
				val dayName = dt.format(dayFormat).replaceFirstChar { it.uppercase() }
				val dayNum = dt.format(dayNumFormat)
				val pair = dayName to dayNum
				if (!daysMap.contains(pair)) {
					daysMap.add(pair)
				}
			}
		}

		val newSelectedDay = if (currentSelectedDay.isNotBlank() && daysMap.any { "${it.first} ${it.second}" == currentSelectedDay }) {
			currentSelectedDay
		} else {
			daysMap.firstOrNull()?.let { "${it.first} ${it.second}" } ?: ""
		}

		return daysMap to newSelectedDay
	}

	private fun loadTutorDetailData(tutorId: String) {
		_state.update { it.copy(isLoading = true, error = null, bookingSuccess = false) }
		viewModelScope.launch {
			try {
				val uuid = runCatching { UUID.fromString(tutorId) }.getOrNull()
				if (uuid != null) {
					val tutorDto = UserProfilesApi.getSelectTutorProfile(uuid)
					val reviewsPage = ReviewsApi.getTutorReviews(uuid, startIndex = 0, size = 10)
					val servicesPage = ServicesApi.getServices(tutorId = uuid, startIndex = 0, size = 10)
					val firstService = servicesPage.items.firstOrNull()

					val freeSchedules = mutableListOf<ScheduleResponse>()
					val serviceUuid = firstService?.idService?.let { runCatching { UUID.fromString(it) }.getOrNull() }
					if (serviceUuid != null) {
						val schedules = runCatching {
							SchedulesApi.getServiceSchedule(serviceUuid, startIndex = 0, size = 50).items.filter { !it.isBooked }
						}.getOrDefault(emptyList())
						freeSchedules.addAll(schedules)
					}

					val (days, defaultDay) = updateSchedulesAndDays(freeSchedules, "")
					val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
					val firstSchedule = freeSchedules.firstOrNull()
					val defaultTime = firstSchedule?.let { parseScheduleDateTime(it.startTime)?.format(timeFormat) } ?: ""

					_state.update {
						it.copy(
							tutor = tutorDto,
							service = firstService,
							services = servicesPage.items,
							availableSchedules = freeSchedules,
							availableDays = days,
							selectedDay = defaultDay,
							selectedScheduleId = firstSchedule?.idSchedule,
							selectedTimeSlot = defaultTime,
							reviews = reviewsPage.items,
							isLoading = false
						)
					}
				} else {
					_state.update { it.copy(isLoading = false, error = "Некорректный ID репетитора") }
				}
			} catch (e: ApiException) {
				Log.e("TutorDetailViewModel", "API Exception: ${e.code} ${e.errorMessage()}", e)
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
						tutor = null,
						error = if (e.code == 401) "Требуется авторизация" else (e.errorMessage() ?: "Ошибка загрузки профиля (${e.code})")
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

	private fun loadServiceSchedule(serviceId: String) {
		val service = _state.value.services.find { it.idService == serviceId } ?: _state.value.service
		if (service == null) {
			loadServiceDetailData(serviceId)
			return
		}
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val serviceUuid = UUID.fromString(service.idService)
				val schedulesPage = runCatching {
					SchedulesApi.getServiceSchedule(serviceUuid, startIndex = 0, size = 50)
				}.getOrNull()
				val freeSchedules = schedulesPage?.items?.filter { !it.isBooked } ?: emptyList()

				val (days, defaultDay) = updateSchedulesAndDays(freeSchedules, "")
				val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
				val firstSchedule = freeSchedules.firstOrNull()
				val defaultTime = firstSchedule?.let { parseScheduleDateTime(it.startTime)?.format(timeFormat) } ?: ""

				_state.update {
					it.copy(
						service = service,
						availableSchedules = freeSchedules,
						availableDays = days,
						selectedDay = defaultDay,
						selectedScheduleId = firstSchedule?.idSchedule,
						selectedTimeSlot = defaultTime,
						isLoading = false
					)
				}
			} catch (e: Exception) {
				Log.e("TutorDetailViewModel", "Error loading service schedule: ${e.message}", e)
				_state.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	private fun loadServiceDetailData(serviceId: String) {
		_state.update { it.copy(isLoading = true, error = null, bookingSuccess = false) }
		viewModelScope.launch {
			try {
				val serviceUuid = runCatching { UUID.fromString(serviceId) }.getOrNull()
				if (serviceUuid != null) {
					val service = ServicesApi.getService(serviceUuid)
					val tutorUuid = runCatching { UUID.fromString(service.tutor.idTutorProfile) }.getOrNull()
					val servicesPage = if (tutorUuid != null) {
						runCatching { ServicesApi.getServices(tutorId = tutorUuid, startIndex = 0, size = 10).items }.getOrDefault(listOf(service))
					} else listOf(service)

					val schedulesPage = runCatching {
						SchedulesApi.getServiceSchedule(serviceUuid, startIndex = 0, size = 50)
					}.getOrNull()
					val freeSchedules = schedulesPage?.items?.filter { !it.isBooked } ?: emptyList()

					val (days, defaultDay) = updateSchedulesAndDays(freeSchedules, "")
					val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
					val firstSchedule = freeSchedules.firstOrNull()
					val defaultTime = firstSchedule?.let { parseScheduleDateTime(it.startTime)?.format(timeFormat) } ?: ""

					val reviewsPage = if (tutorUuid != null) {
						runCatching { ReviewsApi.getTutorReviews(tutorUuid, startIndex = 0, size = 10).items }.getOrDefault(emptyList())
					} else emptyList()

					_state.update {
						it.copy(
							service = service,
							tutor = service.tutor,
							services = servicesPage,
							availableSchedules = freeSchedules,
							availableDays = days,
							selectedDay = defaultDay,
							selectedScheduleId = firstSchedule?.idSchedule,
							selectedTimeSlot = defaultTime,
							reviews = reviewsPage,
							isLoading = false
						)
					}
				} else {
					_state.update { it.copy(isLoading = false, error = "Некорректный ID услуги") }
				}
			} catch (e: ApiException) {
				Log.e("TutorDetailViewModel", "Service API Exception: ${e.code} ${e.errorMessage()}", e)
				if (e.code == 401) {
					ApiClient.clearTokens()
					dataStore.edit { prefs ->
						prefs.remove(DataStoreKeys.ACCESS_TOKEN)
						prefs.remove(DataStoreKeys.REFRESH_TOKEN)
						prefs.remove(DataStoreKeys.IS_TUTOR)
					}
				}
				_state.update { it.copy(isLoading = false, error = if (e.code == 401) "Требуется авторизация" else e.errorMessage()) }
			} catch (e: Exception) {
				Log.e("TutorDetailViewModel", "Error loading service schedule: ${e.message}", e)
				_state.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	private fun confirmBookingData() {
		val selectedScheduleId = _state.value.selectedScheduleId
		val selectedScheduleUuid = selectedScheduleId?.let { runCatching { UUID.fromString(it) }.getOrNull() }

		if (selectedScheduleUuid == null) {
			_state.update { it.copy(error = "Выберите доступный слот из расписания") }
			return
		}

		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				BookingsApi.postBooking(selectedScheduleUuid)
				_state.update { it.copy(isLoading = false, bookingSuccess = true) }
			} catch (e: ApiException) {
				Log.e("TutorDetailViewModel", "Booking API Exception: ${e.code} ${e.errorMessage()}", e)
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
						error = if (e.code == 401) "Требуется авторизация" else (e.errorMessage() ?: "Ошибка бронирования (${e.code})")
					)
				}
			} catch (e: ApiException) {
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
