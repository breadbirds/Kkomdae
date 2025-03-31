package com.pizza.kkomdae.data.repository

import com.pizza.kkomdae.data.model.Step3Mapper
import com.pizza.kkomdae.data.source.remote.Step3Service
import com.pizza.kkomdae.domain.model.PostThirdStageRequest
import com.pizza.kkomdae.domain.repository.Step3Repository
import javax.inject.Inject

class Step3RepositoryImpl@Inject constructor(
    private val step3Service: Step3Service
):Step3Repository {
    override suspend fun postThirdStage(postThirdStageRequest: PostThirdStageRequest): Boolean {
        return try {
                step3Service.postThirdStage(
                    Step3Mapper.toPostThirdStageRequestDto(
                        postThirdStageRequest
                    )
                )


        } catch (e: Exception) {
            throw e
        }
    }
}