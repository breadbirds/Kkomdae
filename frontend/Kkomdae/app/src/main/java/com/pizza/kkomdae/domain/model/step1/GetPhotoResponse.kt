package com.pizza.kkomdae.domain.model.step1

data class GetPhotoResponse(
    val success: Boolean,
    val status: String,
    val message: String,
    val data: Map<String, String>
)
