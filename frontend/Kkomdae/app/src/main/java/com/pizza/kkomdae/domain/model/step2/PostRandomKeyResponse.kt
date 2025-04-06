package com.pizza.kkomdae.domain.model.step2

data class PostRandomKeyResponse(
    val success: Boolean,
    val status: String,
    val message: String,
    val data : RandomKeyData
)
