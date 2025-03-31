package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.TestResponse

interface InspectRepository {
    suspend fun postText(serialNum: String?) : Long
}