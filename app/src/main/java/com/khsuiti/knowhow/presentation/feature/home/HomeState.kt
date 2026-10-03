package com.khsuiti.knowhow.presentation.feature.home

import com.khsuiti.knowhow.responsesData.ServiceResponse
import com.khsuiti.knowhow.responsesData.TutorProfileWithUserResponse

data class HomeState(
	val tutors: List<TutorProfileWithUserResponse> = emptyList(),
	val services: List<ServiceResponse> = emptyList(),
	val popularSubjects: List<String> = listOf("Все эксперты", "Математика", "Физика", "Химия", "Английский"),
	val selectedSubject: String? = null,
	val searchQuery: String = "",
	val isLoading: Boolean = false,
	val error: String? = null,
	val dimmingLevel: Float = 0.15f,
	val isBackgroundAnimationEnabled: Boolean = true
)
