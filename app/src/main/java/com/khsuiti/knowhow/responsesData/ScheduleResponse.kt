package com.khsuiti.knowhow.responsesData

data class ScheduleResponse(
    val idSchedule: String,
    val idService: String,
    val startTime: String,
    val endTime: String,
    val isBooked: Boolean
)
