package com.khsuiti.knowhow.responsesData

data class QualificationDocumentResponse(
    val idQualificationDocuments: String,
    val fileUrl: String,
    val isVerified: Boolean? = null
)
