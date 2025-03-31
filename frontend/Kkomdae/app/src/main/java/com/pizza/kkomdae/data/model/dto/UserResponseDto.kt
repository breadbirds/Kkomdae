package com.pizza.kkomdae.data.model.dto

data class UserResponseDto(
    val onGoingTestId : Int,
    val stage : Int,
    val picStage : Int,
    val name : String,
    val userRentTestRes : List<UserRentTestRes>
)
