package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.GetAiPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.LoginResponseDto
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.LoginResponse

object FinalMapper {

    fun toGetAiPhotoResponse(getAiPhotoResponseDto: GetAiPhotoResponseDto)= GetAiPhotoResponse(
        success = getAiPhotoResponseDto.success,
        status = getAiPhotoResponseDto.status,
        message= getAiPhotoResponseDto.message,
    )
}