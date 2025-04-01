package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.GetAiPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.GetTotalResultResponseDto
import com.pizza.kkomdae.data.model.dto.LoginResponseDto
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.GetTotalResultResponse
import com.pizza.kkomdae.domain.model.LoginResponse

object FinalMapper {

    fun toGetAiPhotoResponse(getAiPhotoResponseDto: GetAiPhotoResponseDto)= GetAiPhotoResponse(
        success = getAiPhotoResponseDto.success,
        status = getAiPhotoResponseDto.status,
        message= getAiPhotoResponseDto.message,
    )


    fun toGetTotalResultResponse(getTotalResultResponseDto: GetTotalResultResponseDto)= GetTotalResultResponse(
        keyboardStatus = getTotalResultResponseDto.keyboardStatus,
        useStatus = getTotalResultResponseDto.useStatus,
        cameraStatus= getTotalResultResponseDto.cameraStatus,
        batteryStatus= getTotalResultResponseDto.batteryStatus,
        chargerStatus= getTotalResultResponseDto.chargerStatus,
        modelCode = getTotalResultResponseDto.modelCode,
        serialNum=getTotalResultResponseDto.serialNum,
        barcodeNum = getTotalResultResponseDto.barcodeNum,
        date=getTotalResultResponseDto.date,
        laptopCount = getTotalResultResponseDto.laptopCount,
        mouseCount=getTotalResultResponseDto.mouseCount,
        powerCableCount = getTotalResultResponseDto.powerCableCount,
        bagCount = getTotalResultResponseDto.bagCount,
        adapterCount = getTotalResultResponseDto.adapterCount,
        mousepadCount = getTotalResultResponseDto.mousepadCount,
        description = getTotalResultResponseDto.description,
        imageNames = getTotalResultResponseDto.imageNames
    )
}