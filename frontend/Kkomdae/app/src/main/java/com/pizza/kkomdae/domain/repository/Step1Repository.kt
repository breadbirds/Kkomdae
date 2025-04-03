package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.GetPhotoResponse
import com.pizza.kkomdae.domain.model.PhotoResponse
import okhttp3.MultipartBody

interface Step1Repository {

    suspend fun postPhoto(
        testId:Long,
        photoType: Int,
        file: MultipartBody.Part,
    ) : PhotoResponse

    suspend fun getPhoto(
        testId:Long,
    ) : GetPhotoResponse
}