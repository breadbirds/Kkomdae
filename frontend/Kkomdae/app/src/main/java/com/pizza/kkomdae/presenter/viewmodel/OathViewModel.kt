package com.pizza.kkomdae.presenter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OathViewModel: ViewModel() {
    private val _isOath1Checked = MutableLiveData<Boolean>()
    val isOath1Checked: LiveData<Boolean>
        get() = _isOath1Checked

    private val _isOath2Checked = MutableLiveData<Boolean>()
    val isOath2Checked: LiveData<Boolean>
        get() = _isOath2Checked

    private val _isOath3Checked = MutableLiveData<Boolean>()
    val isOath3Checked: LiveData<Boolean>
        get() = _isOath3Checked

    private val _isOath4Checked = MutableLiveData<Boolean>()
    val isOath4Checked: LiveData<Boolean>
        get() = _isOath4Checked

    fun setIsOath1Checked(data: Boolean){
        _isOath1Checked.postValue(data)
    }

    fun setIsOath2Checked(data: Boolean){
        _isOath2Checked.postValue(data)
    }


    fun setIsOath3Checked(data: Boolean){
        _isOath3Checked.postValue(data)
    }

    fun setIsOath4Checked(data: Boolean){
        _isOath4Checked.postValue(data)
    }

}