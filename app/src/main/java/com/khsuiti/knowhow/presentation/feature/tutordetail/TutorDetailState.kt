package com.khsuiti.knowhow.presentation.feature.tutordetail

import com.khsuiti.knowhow.responsesData.ReviewResponse
import com.khsuiti.knowhow.responsesData.ServiceResponse
import com.khsuiti.knowhow.responsesData.TutorProfileWithUserResponse

data class TutorDetailState(
	val tutor: TutorProfileWithUserResponse? = null,
	val service: ServiceResponse? = null,
	val availableDays: List<Pair<String, String>> = listOf(
		"Mon" to "18", "Tue" to "19", "Wed" to "20", "Thu" to "21", "Fri" to "22"
	),
	val availableTimeSlots: List<String> = listOf("10:00 AM", "02:30 PM", "04:00 PM", "06:30 PM"),
	val selectedDay: String = "Tue 19",
	val selectedTimeSlot: String = "02:30 PM",
	val reviews: List<ReviewResponse> = emptyList(),
	val bookingSuccess: Boolean = false,
	val isLoading: Boolean = false,
	val error: String? = null
)
