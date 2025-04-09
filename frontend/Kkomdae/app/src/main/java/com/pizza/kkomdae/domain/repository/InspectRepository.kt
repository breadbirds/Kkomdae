package com.pizza.kkomdae.domain.repository

interface InspectRepository {
    suspend fun postTest(rentId: Int?) : Long
}