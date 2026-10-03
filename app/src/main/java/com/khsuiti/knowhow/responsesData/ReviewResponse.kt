package com.khsuiti.knowhow.responsesData

data class ReviewResponse(
    val idReview: String,
    val idBooking: String,
    val rating: Int,
    val comment: String?,
    val student: UserResponse
)
