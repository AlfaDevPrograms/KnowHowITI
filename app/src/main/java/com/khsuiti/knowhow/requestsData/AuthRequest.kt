package com.khsuiti.knowhow.requestsData

data class AuthRequest(
    val loginOrEmail: String,
    val password: String,
    val idDevice: String,
    val isRememberThirtyDays: Boolean
)
