package com.pizza.kkomdae.data.repository

import android.util.Log
import com.pizza.kkomdae.data.model.LoginMapper
import com.pizza.kkomdae.data.source.remote.FinalService
import com.pizza.kkomdae.data.source.remote.LoginService
import com.pizza.kkomdae.domain.repository.FinalRepository
import javax.inject.Inject

class FinalRepositoryImpl@Inject constructor(
    private val finalService: FinalService
):FinalRepository {
    override suspend fun postPdf(testId: Long): Boolean {
        return try {
            finalService.postPdf(testId)
        }catch (e: Exception){
            throw e
        }
    }
}