package com.pizza.kkomdae.data.model.mapper

import com.pizza.kkomdae.data.model.dto.login.LoginResponseDto
import com.pizza.kkomdae.domain.model.login.LoginResponse

object LoginMapper {
    fun toLoginResponse(loginResponseDto: LoginResponseDto)= LoginResponse(
        jwt = loginResponseDto.jwt,
        refreshToken = loginResponseDto.refreshToken
    )
}