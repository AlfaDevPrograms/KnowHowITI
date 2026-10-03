package com.khsuiti.knowhow.requestsData

data class RegisterRequest(
    val login: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null
)
