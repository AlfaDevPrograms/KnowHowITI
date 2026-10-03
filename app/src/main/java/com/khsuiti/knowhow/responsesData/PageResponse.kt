package com.khsuiti.knowhow.responsesData

data class PageResponse<T>(
    val startIndex: Int,
    val endIndex: Int,
    val totalCount: Int,
    val items: List<T>
)
