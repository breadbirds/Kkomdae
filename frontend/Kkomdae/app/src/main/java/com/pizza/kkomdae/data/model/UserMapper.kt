package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.LoginResponseDto
import com.pizza.kkomdae.data.model.dto.UserResponseDto
import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.model.UserResponse

object UserMapper {
    fun toUserResponse(userResponseDto: UserResponseDto)= UserResponse(
        onGoingTestId= userResponseDto.onGoingTestId,
        stage = userResponseDto.stage,
        userRentTestRes = userResponseDto.userRentTestRes,
        picStage = userResponseDto.picStage,
        name = userResponseDto.name,

    )
}