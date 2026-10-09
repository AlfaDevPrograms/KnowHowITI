package com.khsuiti.knowhow.presentation.feature.lessons

import com.khsuiti.knowhow.responsesData.BookingResponse

data class LessonsState(
	val bookings: List<BookingResponse> = emptyList(),
	val isLoading: Boolean = false,
	val error: String? = null
)
