package com.khsuiti.knowhow.responsesData

data class BookingResponse(
    val idBooking: String,
    val idStudentProfile: String,
    val student: UserResponse,
    val isClose: Boolean,
    val dateBooking: String,
    val schedule: ScheduleResponse,
    val service: ServiceResponse
)
