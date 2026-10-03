package com.khsuiti.knowhow.presentation.feature.profile

import com.khsuiti.knowhow.responsesData.AccountInfoResponse

data class ProfileState(
	val accountInfo: AccountInfoResponse? = null,
	val completedLessonsCount: Int = 0,
	val balanceLessonsCount: Int = 0,
	val activeCoursesCount: Int = 0,
	val paymentMethod: String = "Visa •••• 4242",
	val notificationsEnabled: Boolean = true,
	val isLoading: Boolean = false,
	val error: String? = null
)
