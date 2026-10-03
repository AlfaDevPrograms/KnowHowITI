package com.khsuiti.knowhow.responsesData

import java.math.BigDecimal

data class ServiceResponse(
    val idService: String,
    val idSubject: String,
    val subjectName: String,
    val description: String?,
    val lessonDurationMinutes: Int,
    val priceInDollars: BigDecimal,
    val isOnlineFormat: Boolean,
    val isActive: Boolean,
    val tutor: TutorProfileWithUserResponse
)
