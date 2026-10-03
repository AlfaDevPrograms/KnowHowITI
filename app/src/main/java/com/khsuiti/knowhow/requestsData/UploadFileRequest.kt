package com.khsuiti.knowhow.requestsData

import okhttp3.MultipartBody

data class UploadFileRequest(
    val file: MultipartBody.Part
)
