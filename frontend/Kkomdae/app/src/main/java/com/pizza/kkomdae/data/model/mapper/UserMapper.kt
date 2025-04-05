package com.pizza.kkomdae.data.model.mapper

import com.pizza.kkomdae.data.model.dto.user.UserResponseDto
import com.pizza.kkomdae.domain.model.user.UserResponse

object UserMapper {
    fun toUserResponse(userResponseDto: UserResponseDto)= UserResponse(
        onGoingTestId= userResponseDto.onGoingTestId,
        stage = userResponseDto.stage,
        userRentTestRes = userResponseDto.userRentTestRes,
        picStage = userResponseDto.picStage,
        name = userResponseDto.name,

    )
}