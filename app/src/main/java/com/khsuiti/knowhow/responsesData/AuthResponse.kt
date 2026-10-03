package com.khsuiti.knowhow.responsesData

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val accountInfo: AccountInfoResponse
)
