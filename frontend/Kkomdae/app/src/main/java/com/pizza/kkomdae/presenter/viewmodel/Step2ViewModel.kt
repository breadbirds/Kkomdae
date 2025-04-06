package com.pizza.kkomdae.presenter.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizza.kkomdae.domain.model.step2.GetStep2ResultResponse
import com.pizza.kkomdae.domain.model.step2.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.step2.PostResponse
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

    private var _randomKey = MutableLiveData<String>()
    val randomKey: LiveData<String>
        get() = _randomKey

    private var _getStep2Result = MutableLiveData<GetStep2ResultResponse>()
    val getStep2Result: LiveData<GetStep2ResultResponse>
        get() = _getStep2Result




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

    // 랜덤키 생성
    fun postRandomKey(){
        viewModelScope.launch {
            val result = step2UseCase.postRandomKey(testId = sharedPreferences.getLong("test_id",0))
            result.onSuccess {
                _randomKey.postValue(it.data.randomKey)
            }
        }
    }

    suspend fun getStep2Result(): Result<GetStep2ResultResponse>{

          return try {
              step2UseCase.getStep2Result(testId = sharedPreferences.getLong("test_id",0))
          }catch (e: Exception){
              Result.failure(e)
          }

    }

    suspend fun postSecondToThird():Result<PostResponse>{
        return try {
            step2UseCase.postSecondToThird(testId = sharedPreferences.getLong("test_id",0))
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun postSecondStage():Result<PostResponse>{

            return try {
                step2UseCase.postSecondStage(
                    PostSecondStageRequest(
                    testId = sharedPreferences.getLong("test_id",0),
                    keyboardStatus = keyboardStatus.value?.status == "pass",
                    failedKeys = keyboardStatus.value?.failed_keys?.joinToString("@")?:"",
                    usbStatus = usbStatus.value?.status== "pass",
                    failedPorts = usbStatus.value?.failed_ports?.joinToString("@")?:"",
                    cameraStatus = cameraStatus.value?.status == "pass",
                    chargerStatus = chargerStatus.value?.status == "pass",
                    batteryReport = batteryStatus.value?.status == "pass",
                    batteryReportUrl =batteryStatus.value?.reportName ?:""
                )
                )
            }catch (e:Exception){
                Result.failure(e)
            }




    }

}