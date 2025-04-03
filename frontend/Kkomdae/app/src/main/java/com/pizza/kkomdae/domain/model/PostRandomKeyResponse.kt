package com.pizza.kkomdae.domain.model

import com.pizza.kkomdae.data.model.dto.RandomKeyDataDto

data class PostRandomKeyResponse(
    val success: Boolean,
    val status: String,
    val message: String,
    val data : RandomKeyData
)
