package com.khsuiti.knowhow.requestsData

data class ReviewRequest(
    val idBooking: String,
    val rating: Int,
    val comment: String?
)
