package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.GetPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.PhotoResponseDto
import com.pizza.kkomdae.domain.model.GetPhotoResponse
import com.pizza.kkomdae.domain.model.PhotoResponse

object Step1Mapper {
    fun toPhotoResponse(photoResponseDto: PhotoResponseDto)= PhotoResponse(
        success = photoResponseDto.success,
        message = photoResponseDto.message
        )

    fun toGetPhotoResponse(getPhotoResponseDto: GetPhotoResponseDto)= GetPhotoResponse(
        success = getPhotoResponseDto.success,
        message = getPhotoResponseDto.message,
        status = getPhotoResponseDto.status,
        data = getPhotoResponseDto.data
    )
}