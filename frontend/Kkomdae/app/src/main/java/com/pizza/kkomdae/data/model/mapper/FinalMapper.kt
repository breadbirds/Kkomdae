package com.pizza.kkomdae.data.model.mapper

import com.pizza.kkomdae.data.model.dto.step4.AiPhotoDataDto
import com.pizza.kkomdae.data.model.dto.step4.FourthStageRequestDto
import com.pizza.kkomdae.data.model.dto.step4.GetAiPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.step4.GetPdUrlResponseDto
import com.pizza.kkomdae.data.model.dto.step4.GetTotalResultResponseDto
import com.pizza.kkomdae.data.model.dto.step4.PostRePhotoResponseDto
import com.pizza.kkomdae.data.model.dto.step4.RePhotoDataDto
import com.pizza.kkomdae.domain.model.step4.AiPhotoData
import com.pizza.kkomdae.domain.model.step4.FourthStageRequest
import com.pizza.kkomdae.domain.model.step4.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.step4.GetTotalResultResponse
import com.pizza.kkomdae.domain.model.step4.GetPdfUrlResponse
import com.pizza.kkomdae.domain.model.step4.PostRePhotoResponse
import com.pizza.kkomdae.domain.model.step4.RePhotoData

object FinalMapper {

    fun toGetAiPhotoResponse(getAiPhotoResponseDto: GetAiPhotoResponseDto)= GetAiPhotoResponse(
        success = getAiPhotoResponseDto.success,
        status = getAiPhotoResponseDto.status,
        message= getAiPhotoResponseDto.message,
        data = toApiPhotoData(getAiPhotoResponseDto.data),

    )

    fun toFourthStageRequestDto(fourthStageRequest: FourthStageRequest)= FourthStageRequestDto(
        testId =  fourthStageRequest.testId,
        description = fourthStageRequest.description
    )

    fun toGetPdfUrlDto(getPdUrlResponseDto: GetPdUrlResponseDto)= GetPdfUrlResponse(
        url =  getPdUrlResponseDto.url,
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
        photo1_ai_damage = apiPhotoDataDto.photo1_ai_damage,
        photo2_ai_damage = apiPhotoDataDto.photo2_ai_damage,
        photo3_ai_damage=apiPhotoDataDto.photo3_ai_damage,
        photo4_ai_damage = apiPhotoDataDto.photo4_ai_damage,
        photo5_ai_damage = apiPhotoDataDto.photo5_ai_damage,
        photo6_ai_damage = apiPhotoDataDto.photo6_ai_damage
    )

    fun toPostRePhotoResponse(postRePhotoResponseDto: PostRePhotoResponseDto) = PostRePhotoResponse(
        success = postRePhotoResponseDto.success,
        status = postRePhotoResponseDto.status,
        message = postRePhotoResponseDto.message,
        data = toRePhotoData(postRePhotoResponseDto.data)
    )

    fun toRePhotoData(rePhotoDataDto: RePhotoDataDto) = RePhotoData(
       photo_ai_damage =  rePhotoDataDto.photo_ai_damage,
        photo_ai_name = rePhotoDataDto.photo_ai_name,
        photo_ai_url = rePhotoDataDto.photo_ai_url
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
        imageUrls = getTotalResultResponseDto.imageUrls
    )
}