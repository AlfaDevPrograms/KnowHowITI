package com.khsuiti.knowhow.responsesData

data class TutorProfileWithUserResponse(
    val idTutorProfile: String,
    val experienceYear: Int,
    val createdDate: String,
    val isVerified: Boolean,
    val isDeleted: Boolean,
    val user: UserResponse,
    val bio: String?,
    val photoURL: String?,
    val education: EducationResponse,
    val averageRating: Double?,
    val reviewCount: Int,
    val reviews: PageResponse<ReviewResponse>
)
