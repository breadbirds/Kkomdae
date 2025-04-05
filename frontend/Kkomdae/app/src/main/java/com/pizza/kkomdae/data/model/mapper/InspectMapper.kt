package com.pizza.kkomdae.data.model.mapper

import com.pizza.kkomdae.data.model.dto.user.TestResponseDto
import com.pizza.kkomdae.domain.model.user.TestResponse

object InspectMapper {

    fun toInspectResponse(testResponseDto: TestResponseDto)= TestResponse(
        testId = testResponseDto.testId,
        serialNum = testResponseDto.serialNum
    )
}