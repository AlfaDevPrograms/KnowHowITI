package com.khsuiti.knowhow.responsesData

data class ErrorResponse(
    val code: String,
    val message: String? = null,
    val leftAttempts: Long? = null
)
