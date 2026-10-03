package com.khsuiti.knowhow.presentation.feature.catalog

import com.khsuiti.knowhow.responsesData.ServiceResponse

data class SubjectCatalogItem(
	val service: ServiceResponse,
	val subjectName: String,
	val description: String,
	val rating: Double,
	val reviewCount: Int,
	val subtopics: List<String>,
	val tutorCount: Int,
	val gradeLevel: String,
	val photoUrl: String
)

data class CatalogState(
	val subjects: List<SubjectCatalogItem> = emptyList(),
	val popularCategories: List<String> = listOf("Все предметы", "Математика", "Физика", "Химия", "Английский"),
	val selectedCategory: String? = null,
	val searchQuery: String = "",
	val isLoading: Boolean = false,
	val error: String? = null
)
