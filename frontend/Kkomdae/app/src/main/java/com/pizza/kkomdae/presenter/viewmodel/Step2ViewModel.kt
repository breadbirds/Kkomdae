package com.pizza.kkomdae.presenter.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.usecase.Step2UseCase
import com.pizza.kkomdae.presenter.model.BatteryReport
import com.pizza.kkomdae.presenter.model.ComponentStatus
import com.pizza.kkomdae.presenter.model.KeyboardStatus
import com.pizza.kkomdae.presenter.model.UsbStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "Step2ViewModel"
@HiltViewModel
class Step2ViewModel@Inject constructor(
    application: Application,
    private val step2UseCase: Step2UseCase,
): ViewModel() {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

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

    private var _batteryStatus = MutableLiveData<BatteryReport>()
    val batteryStatus: LiveData<BatteryReport>
        get() = _batteryStatus

    private var _postResponse = MutableLiveData<PostResponse>()
    val postResponse: LiveData<PostResponse>
        get() = _postResponse




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

    fun setBatteryStatus(data: BatteryReport) {
        _batteryStatus.value = data
    }


    fun postSecondStage(){
        viewModelScope.launch {
            val result = step2UseCase.postSecondStage(PostSecondStageRequest(
                testId = sharedPreferences.getLong("test_id",0),
                keyboardStatus = keyboardStatus.value?.status == "pass",
                failedKeys = keyboardStatus.value?.failed_keys?.joinToString("@")?:"",
                usbStatus = usbStatus.value?.status== "pass",
                failedPorts = usbStatus.value?.failed_ports?.joinToString("@")?:"",
                cameraStatus = cameraStatus.value?.status == "pass",
                chargerStatus = chargerStatus.value?.status == "pass",
                batteryReport = batteryStatus.value?.status == "pass",
                batteryReportUrl =batteryStatus.value?.reportName ?:""

            ))
            Log.d(TAG, "getUserInfo: $result")

            result.onSuccess { response ->
                // 로그인 성공 시 실제 데이터 처리
                response?.let {
                    _postResponse.postValue(it)
                }


            }.onFailure { exception ->
                // 로그인 정보 불러오기 실패

            }

        }
    }

}