package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.TestResponseDto
import com.pizza.kkomdae.data.model.dto.UserResponseDto
import com.pizza.kkomdae.domain.model.TestResponse
import com.pizza.kkomdae.domain.model.UserResponse

object InspectMapper {

    fun toInspectResponse(testResponseDto: TestResponseDto)= TestResponse(
        testId = testResponseDto.testId,
        serialNum = testResponseDto.serialNum
    )
}