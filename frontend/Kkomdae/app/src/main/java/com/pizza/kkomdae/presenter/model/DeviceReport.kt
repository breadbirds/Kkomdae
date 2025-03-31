package com.pizza.kkomdae.presenter.model

data class DeviceReport(
    val keyboard: KeyboardStatus,
    val usb: UsbStatus,
    val camera: ComponentStatus,
    val charger: ComponentStatus,
    val battery_report: String
)
data class KeyboardStatus(
    val status: String,
    val failed_keys: List<String>
)

data class UsbStatus(
    val status: String,
    val failed_ports: List<String>
)

data class ComponentStatus(
    val status: String
)
