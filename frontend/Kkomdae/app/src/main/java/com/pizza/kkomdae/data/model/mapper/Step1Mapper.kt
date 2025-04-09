package com.pizza.kkomdae.data.model.mapper

import com.pizza.kkomdae.data.model.dto.step1.GetPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.step1.PhotoResponseDto
import com.pizza.kkomdae.domain.model.step1.GetPhotoResponse
import com.pizza.kkomdae.domain.model.step1.PhotoResponse

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