package com.pizza.kkomdae.data.repository

import com.pizza.kkomdae.data.model.Step1Mapper
import com.pizza.kkomdae.data.model.UserMapper
import com.pizza.kkomdae.data.source.remote.Step1Service
import com.pizza.kkomdae.data.source.remote.UserService
import com.pizza.kkomdae.domain.model.GetPhotoResponse
import com.pizza.kkomdae.domain.model.PhotoResponse
import com.pizza.kkomdae.domain.repository.Step1Repository
import okhttp3.MultipartBody
import javax.inject.Inject

class Step1RepositoryImpl@Inject constructor(
    private val step1Service: Step1Service
):Step1Repository {
    override suspend fun postPhoto(
        testId: Long,
        photoType: Int,
        file: MultipartBody.Part,
    ): PhotoResponse {
        return try {
            Step1Mapper.toPhotoResponse(step1Service.postPhoto(
                file = file,
                photoType = photoType,
                testId = testId
                ))
        }catch (e: Exception){
            throw e
        }
    }

    override suspend fun getPhoto(testId: Long): GetPhotoResponse {
        return try {
            Step1Mapper.toGetPhotoResponse(step1Service.getPhoto(testId))
        }catch (e:Exception){
            throw e
        }
    }
}