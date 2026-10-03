package com.khsuiti.knowhow.responsesData

data class AdminUserResponse(
    val account: AccountInfoResponse,
    val registrationDate: String,
    val studentProfile: StudentProfileResponse?,
    val tutorProfile: TutorProfileWithUserResponse?
)
