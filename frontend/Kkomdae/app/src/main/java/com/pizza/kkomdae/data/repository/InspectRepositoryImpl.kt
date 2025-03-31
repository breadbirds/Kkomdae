package com.pizza.kkomdae.data.repository

import com.pizza.kkomdae.data.model.InspectMapper
import com.pizza.kkomdae.data.model.UserMapper
import com.pizza.kkomdae.data.source.remote.InspectService
import com.pizza.kkomdae.data.source.remote.UserService
import com.pizza.kkomdae.domain.model.TestResponse
import com.pizza.kkomdae.domain.repository.InspectRepository
import javax.inject.Inject

class InspectRepositoryImpl@Inject constructor(
    private val inspectService: InspectService
) : InspectRepository {
    override suspend fun postText(serialNum: String?): Long {
        return try {
            inspectService.postTest(serialNum)
//            InspectMapper.toInspectResponse(inspectService.postTest(serialNum))
        }catch (e: Exception){
            throw e
        }
    }


}