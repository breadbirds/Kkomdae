package com.pizza.kkomdae.ui.step2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pizza.kkomdae.presenter.model.ComponentStatus
import com.pizza.kkomdae.presenter.model.KeyboardStatus
import com.pizza.kkomdae.presenter.model.UsbStatus

class Step2ViewModel: ViewModel() {

    private var _keyboardStatus = MutableLiveData<KeyboardStatus>()
    val keyboardStatus: LiveData<KeyboardStatus>
        get() = _keyboardStatus

    private var _usbStatus = MutableLiveData<UsbStatus>()
    val usbStatus: LiveData<UsbStatus>
        get() = _usbStatus

    private var _cameraStatus = MutableLiveData<ComponentStatus>()
    val cameraStatus: LiveData<ComponentStatus>
        get() = _cameraStatus

    private var _chargerStatus = MutableLiveData<ComponentStatus>()
    val chargerStatus: LiveData<ComponentStatus>
        get() = _chargerStatus

    private var _batteryStatus = MutableLiveData<String>()
    val batteryStatus: LiveData<String>
        get() = _batteryStatus


    fun setKeyboardStatus(data: KeyboardStatus) {
        _keyboardStatus.value = data
    }

    fun setUsbStatus(data: UsbStatus) {
        _usbStatus.value = data
    }

    fun setCameraStatus(data: ComponentStatus) {
        _cameraStatus.value = data
    }

    fun setChargerStatus(data: ComponentStatus) {
        _chargerStatus.value = data
    }

    fun setBatteryStatus(data: String) {
        _batteryStatus.value = data
    }

}