package com.pizza.kkomdae.data.repository

import com.pizza.kkomdae.data.source.remote.InspectService
import com.pizza.kkomdae.domain.repository.InspectRepository
import javax.inject.Inject

class InspectRepositoryImpl@Inject constructor(
    private val inspectService: InspectService
) : InspectRepository {
    override suspend fun postTest(rentId: Int?): Long {
        return try {
            inspectService.postTest(rentId)
//            InspectMapper.toInspectResponse(inspectService.postTest(serialNum))
        }catch (e: Exception){
            throw e
        }
    }


}