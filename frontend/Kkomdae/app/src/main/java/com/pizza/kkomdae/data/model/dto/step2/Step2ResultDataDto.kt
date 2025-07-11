package com.pizza.kkomdae.data.model.dto.step2

data class Step2ResultDataDto(
    val battery_report: Boolean?,
    val camera_status: Boolean?,
    val charging_status: Boolean?,
    val keyboard_status: Boolean?,
    val usb_status: Boolean?,
    val failed_keys: String?,
    val battery_report_url: String?,
    val failed_ports: String?,
    val success: Boolean?
)
