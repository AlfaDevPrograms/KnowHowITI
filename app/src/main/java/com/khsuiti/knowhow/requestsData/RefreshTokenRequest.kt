package com.khsuiti.knowhow.requestsData

data class RefreshTokenRequest(
    val accessToken: String,
    val refreshToken: String
)
