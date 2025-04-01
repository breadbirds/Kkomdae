package com.pizza.kkomdae.data.repository

import com.pizza.kkomdae.data.model.Step2Mapper
import com.pizza.kkomdae.data.source.remote.Step2Service
import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.repository.Step2Repository
import javax.inject.Inject

class Step2RepositoryImpl @Inject constructor(
    private val step2Service: Step2Service
) : Step2Repository {
    override suspend fun postSecondStage(postSecondStageRequest: PostSecondStageRequest): PostResponse {
        return try {
            Step2Mapper.toPostStageResponse(
                step2Service.postSecondStage(
                    Step2Mapper.toPostSecondStageRequestDto(
                        postSecondStageRequest
                    )
                )
            )

        } catch (e: Exception) {
            throw e
        }
    }

}