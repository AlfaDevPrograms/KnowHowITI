package com.khsuiti.knowhow.presentation.feature.tutordetail

sealed interface TutorDetailIntent {
	data class LoadTutorDetail(val tutorId: String) : TutorDetailIntent
	data class LoadServiceDetail(val serviceId: String) : TutorDetailIntent
	data class SelectDate(val date: String) : TutorDetailIntent
	data class SelectTimeSlot(val timeSlot: String) : TutorDetailIntent
	data class SelectScheduleSlot(val scheduleId: String, val timeText: String) : TutorDetailIntent
	data object ConfirmBooking : TutorDetailIntent
	data object CancelBooking : TutorDetailIntent
}
