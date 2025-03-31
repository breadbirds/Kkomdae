package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.LoginResponseDto
import com.pizza.kkomdae.domain.model.LoginResponse

object LoginMapper {
    fun toLoginResponse(loginResponseDto: LoginResponseDto)= LoginResponse(
        jwt = loginResponseDto.jwt,
        refreshToken = loginResponseDto.refreshToken
    )
}