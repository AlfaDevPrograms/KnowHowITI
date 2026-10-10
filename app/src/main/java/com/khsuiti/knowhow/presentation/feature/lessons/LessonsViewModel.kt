package com.khsuiti.knowhow.presentation.feature.lessons

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khsuiti.knowhow.data.local.ApiClient
import com.khsuiti.knowhow.data.local.BookingsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor() : ViewModel() {

	private val _state = MutableStateFlow(LessonsState())
	val state: StateFlow<LessonsState> = _state.asStateFlow()

	init {
		loadBookings()
	}

	fun clearState() {
		_state.update { LessonsState() }
	}

	fun loadBookings() {
		if (ApiClient.accessToken.isNullOrBlank()) return
		_state.update { it.copy(isLoading = true, error = null) }
		viewModelScope.launch {
			try {
				val page = BookingsApi.getMyStudentBookings(startIndex = 0, size = 50)
				_state.update {
					it.copy(
						bookings = page.items,
						isLoading = false
					)
				}
			} catch (e: Exception) {
				Log.e("LessonsViewModel", "Error loading student bookings: ${e.message}", e)
				_state.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun deleteBooking(idBooking: UUID) {
		viewModelScope.launch {
			try {
				BookingsApi.deleteBooking(idBooking)
				loadBookings()
			} catch (e: Exception) {
				Log.e("LessonsViewModel", "Error deleting booking: ${e.message}", e)
				_state.update { it.copy(error = e.message) }
			}
		}
	}
}
