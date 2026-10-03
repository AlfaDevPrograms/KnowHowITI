package com.khsuiti.knowhow.presentation.feature.catalog

sealed interface CatalogIntent {
	data object LoadCatalog : CatalogIntent
	data class SearchQueryChanged(val query: String) : CatalogIntent
	data class SelectCategoryFilter(val category: String?) : CatalogIntent
	data class SelectSubject(val subjectId: String) : CatalogIntent
}
