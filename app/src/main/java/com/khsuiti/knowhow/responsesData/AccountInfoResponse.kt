package com.khsuiti.knowhow.responsesData

data class AccountInfoResponse(
    val idUser: String,
    val login: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val roleID: String,
    val roleName: String,
    val isDiactivated: Boolean,
    val diactivatedDate: String? = null,
    val diactivatedReason: String? = ""
)
