package com.pizza.kkomdae.data.repository

import android.util.Log
import com.pizza.kkomdae.data.model.FinalMapper
import com.pizza.kkomdae.data.model.LoginMapper
import com.pizza.kkomdae.data.model.Step2Mapper
import com.pizza.kkomdae.data.source.remote.FinalService
import com.pizza.kkomdae.data.source.remote.LoginService
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.repository.FinalRepository
import javax.inject.Inject

class FinalRepositoryImpl@Inject constructor(
    private val finalService: FinalService
):FinalRepository {
    override suspend fun postPdf(testId: Long): PostResponse {
        return try {
            Step2Mapper.toPostStageResponse(finalService.postPdf(testId))
        }catch (e: Exception){
            throw e
        }
    }

    override suspend fun getAiPhoto(testId: Long): GetAiPhotoResponse {
        return try {
            FinalMapper.toGetAiPhotoResponse(finalService.getAiPhoto(testId))
        }catch (e: Exception){
            throw e
        }
    }
}