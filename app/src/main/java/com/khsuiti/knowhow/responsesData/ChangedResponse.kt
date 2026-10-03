package com.khsuiti.knowhow.responsesData

data class ChangedResponse(
    val resource: String,
    val id: String,
    val operation: String,
    val parentId: String? = null
)
