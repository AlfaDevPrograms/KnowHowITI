package com.khsuiti.knowhow.presentation.feature.home

sealed interface HomeIntent {
	data object LoadData : HomeIntent
	data class SearchQueryChanged(val query: String) : HomeIntent
	data class SelectSubjectFilter(val subject: String?) : HomeIntent
}
