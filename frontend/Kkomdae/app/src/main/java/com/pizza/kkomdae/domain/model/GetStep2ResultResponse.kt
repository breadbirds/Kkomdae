package com.pizza.kkomdae.domain.model


data class GetStep2ResultResponse(
    val success: Boolean,
    val status : String,
    val message: String,
    val data : Step2ResultData
)
