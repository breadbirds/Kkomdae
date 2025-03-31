package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.LoginResponse

interface FinalRepository {

    suspend fun postPdf(testId: Long) : Boolean
}