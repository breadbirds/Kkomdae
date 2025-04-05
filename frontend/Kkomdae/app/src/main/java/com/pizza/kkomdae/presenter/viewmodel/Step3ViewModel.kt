package com.pizza.kkomdae.presenter.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizza.kkomdae.domain.model.step2.PostResponse
import com.pizza.kkomdae.domain.model.step3.PostThirdStageRequest
import com.pizza.kkomdae.domain.usecase.Step3UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "Step3ViewModel"
@HiltViewModel
class Step3ViewModel@Inject constructor(
    application: Application,
    private val step3UseCase: Step3UseCase,
): ViewModel() {
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private var _modelCode = MutableLiveData<String>()
    val modelCode: LiveData<String>
        get() = _modelCode

    private var _serialNum = MutableLiveData<String>()
    val serialNum: LiveData<String>
        get() = _serialNum

    private var _barcodeNum = MutableLiveData<String>()
    val barcodeNum: LiveData<String>
        get() = _barcodeNum

    private var _localDate = MutableLiveData<String>()
    val localDate: LiveData<String>
        get() = _localDate

    private var _laptop = MutableLiveData<Int>()
    val laptop: LiveData<Int>
        get() = _laptop

    private var _powerCable = MutableLiveData<Int>()
    val powerCable: LiveData<Int>
        get() = _powerCable

    private var _adapter = MutableLiveData<Int>()
    val adapter: LiveData<Int>
        get() = _adapter

    private var _mouse = MutableLiveData<Int>()
    val mouse: LiveData<Int>
        get() = _mouse

    private var _bag = MutableLiveData<Int>()
    val bag: LiveData<Int>
        get() = _bag

    private var _mousePad = MutableLiveData<Int>()
    val mousePad: LiveData<Int>
        get() = _mousePad

    private var _postResponse = MutableLiveData<PostResponse>()
    val postResponse: LiveData<PostResponse>
        get() = _postResponse


    fun clearPostResponse(){
        _postResponse.postValue(null)
    }

    fun setModelCode(data: String) {
        _modelCode.value = data
    }

    fun setSerialNum(data: String) {
        _serialNum.value = data
    }

    fun setBarcodeNum(data: String) {
        _barcodeNum.value = data
    }


    fun setLocalDate(data: String) {
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//        _localDate.value = LocalDate.parse(data, formatter)
        _localDate.value =data
    }

    fun setLaptop(data: Int) {
        _laptop.value = data
    }

    fun setPowerCable(data: Int) {
        _powerCable.value = data
    }
    fun setAdapter(data: Int) {
        _adapter.value = data
    }
    fun setMouse(data: Int) {
        _mouse.value = data
    }

    fun setBag(data: Int) {
        _bag.value = data
    }

    fun setMousePad(data: Int) {
        _mousePad.value = data
    }



    fun postThirdStage(release: Boolean){

        viewModelScope.launch {
            val result = step3UseCase.postThirdStage(
                postThirdStageRequest = PostThirdStageRequest(
                    testId = sharedPreferences.getLong("test_id", 0),
                    // todo 반납때 수정 필요
                    release = release,
                    modelCode = modelCode.value ?: "",
                    serialNum = serialNum.value ?: "",
                    barcodeNum = barcodeNum.value ?: "",
                    localDate = localDate.value?:"",
                    laptop = laptop.value ?: 1,
                    powerCable = powerCable.value ?: 1,
                    adapter = adapter.value ?: 1,
                    mouse = mouse.value ?: 1,
                    bag = bag.value ?: 1,
                    mousePad = mousePad.value ?: 1
                )
            )

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