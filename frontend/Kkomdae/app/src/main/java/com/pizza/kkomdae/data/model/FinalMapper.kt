package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.AiPhotoDataDto
import com.pizza.kkomdae.data.model.dto.GetAiPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.GetTotalResultResponseDto
import com.pizza.kkomdae.domain.model.AiPhotoData
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.GetTotalResultResponse

object FinalMapper {

    fun toGetAiPhotoResponse(getAiPhotoResponseDto: GetAiPhotoResponseDto)= GetAiPhotoResponse(
        success = getAiPhotoResponseDto.success,
        status = getAiPhotoResponseDto.status,
        message= getAiPhotoResponseDto.message,
        data = toApiPhotoData(getAiPhotoResponseDto.data),

    )

    fun toApiPhotoData(apiPhotoDataDto: AiPhotoDataDto)= AiPhotoData(
        Picture1_ai_url=apiPhotoDataDto.photo1_ai_url,
        Picture1_ai_name = apiPhotoDataDto.photo1_ai_name,
        Picture2_ai_url=apiPhotoDataDto.photo2_ai_url,
        Picture2_ai_name = apiPhotoDataDto.photo2_ai_name,
        Picture3_ai_url=apiPhotoDataDto.photo3_ai_url,
        Picture3_ai_name = apiPhotoDataDto.photo3_ai_name,
        Picture4_ai_url=apiPhotoDataDto.photo4_ai_url,
        Picture4_ai_name = apiPhotoDataDto.photo4_ai_name,
        Picture5_ai_url=apiPhotoDataDto.photo5_ai_url,
        Picture5_ai_name = apiPhotoDataDto.photo5_ai_name,
        Picture6_ai_url=apiPhotoDataDto.photo6_ai_url,
        Picture6_ai_name = apiPhotoDataDto.photo6_ai_name,
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