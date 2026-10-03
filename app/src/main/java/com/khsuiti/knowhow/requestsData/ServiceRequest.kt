package com.khsuiti.knowhow.requestsData

import java.math.BigDecimal

data class ServiceRequest(
    val idSubject: String,
    val lessonDurationMinutes: Int,
    val priceInDollars: BigDecimal,
    val isOnlineFormat: Boolean,
    val description: String?
)
