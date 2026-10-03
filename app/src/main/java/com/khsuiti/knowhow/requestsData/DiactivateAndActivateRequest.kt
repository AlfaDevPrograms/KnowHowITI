package com.khsuiti.knowhow.requestsData

data class DiactivateAndActivateRequest(
    val idUser: String,
    val reason: String = ""
)
