package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.GetPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.PhotoResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface Step1Service {

    @Multipart
    @POST("api/photo")
    suspend fun postPhoto(
        @Part file: MultipartBody.Part,
        @Query("photoType") photoType: Int,
        @Query("testId") testId: Long
    ): PhotoResponseDto


    @GET("api/photo")
    suspend fun getPhoto(
        @Query("testId") testId: Long
    ): GetPhotoResponseDto
}