package com.khsuiti.knowhow.presentation.feature.tutordetail

sealed interface TutorDetailIntent {
	data class LoadTutorDetail(val tutorId: String) : TutorDetailIntent
	data class SelectDate(val date: String) : TutorDetailIntent
	data class SelectTimeSlot(val timeSlot: String) : TutorDetailIntent
	data object ConfirmBooking : TutorDetailIntent
	data object CancelBooking : TutorDetailIntent
}
