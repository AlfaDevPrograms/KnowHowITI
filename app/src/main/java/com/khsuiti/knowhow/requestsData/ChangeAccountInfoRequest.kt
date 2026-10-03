package com.khsuiti.knowhow.requestsData

data class ChangeAccountInfoRequest(
    val login: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null
)
