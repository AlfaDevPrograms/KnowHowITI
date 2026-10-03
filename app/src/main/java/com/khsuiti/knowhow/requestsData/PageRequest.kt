package com.khsuiti.knowhow.requestsData

data class PageRequest(
    val startIndex: Int = 0,
    val endIndex: Int = 50
) {
    val size: Int
        get() = endIndex - startIndex
}
