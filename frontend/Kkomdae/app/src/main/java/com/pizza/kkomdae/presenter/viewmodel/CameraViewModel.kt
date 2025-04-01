package com.pizza.kkomdae.presenter.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizza.kkomdae.domain.model.PhotoResponse
import com.pizza.kkomdae.domain.usecase.LoginUseCase
import com.pizza.kkomdae.domain.usecase.Step1UseCase
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val step1UseCase: Step1UseCase,
    application: Application,
) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)


    private val _myPageOrderId = MutableLiveData<Int>()
    val myPageOrderId: LiveData<Int>
        get() = _myPageOrderId

    private val _step = MutableLiveData<Int?>()
    val step: LiveData<Int?>
        get() = _step

    private val _postResult= MutableLiveData<PhotoResponse?>()
    val postResult: LiveData<PhotoResponse?>
        get() = _postResult


    private val _frontUri = MutableLiveData<Uri?>()
    val frontUri: LiveData<Uri?>
        get() = _frontUri

    private val _backUri = MutableLiveData<Uri?>()
    val backUri: LiveData<Uri?>
        get() = _backUri

    private val _leftUri = MutableLiveData<Uri?>()
    val leftUri: LiveData<Uri?>
        get() = _leftUri

    private val _rightUri = MutableLiveData<Uri?>()
    val rightUri: LiveData<Uri?>
        get() = _rightUri

    private val _screenUri = MutableLiveData<Uri?>()
    val screenUri: LiveData<Uri?>
        get() = _screenUri

    private val _keypadUri = MutableLiveData<Uri?>()
    val keypadUri: LiveData<Uri?>
        get() = _keypadUri

    // ✅ 사진 저장 메서드
    fun setFront(uri: Uri) {
        _frontUri.value = uri
    }

    fun setBack(uri: Uri) {
        _backUri.value = uri
    }

    fun setLeft(uri: Uri) {
        _leftUri.value = uri
    }

    fun setRight(uri: Uri) {
        _rightUri.value = uri
    }

    fun setScreen(uri: Uri) {
        _screenUri.value = uri
    }

    fun setKeypad(uri: Uri) {
        _keypadUri.value = uri
    }

    // ✅ 사진 저장 메서드
    fun setStep(step: Int) {
        _step.value = step
        savePhotoStage(step)

    }

    fun clearResult(){
        _postResult.postValue(null)
    }

    fun postPhoto(){
        var uri = frontUri.value
//        val testId = 2L
        val testId = sharedPreferences.getLong("test_id",0)
        Log.d("Post", "postPhoto: ${step.value}")
        when(step.value){
            1->{
                uri=frontUri.value
            }
            2->{
                uri=backUri.value
            }
            3->{
                uri=leftUri.value
            }
            4->{
                uri=rightUri.value
            }
            5->{
                uri=screenUri.value
            }
            6->{
                uri=keypadUri.value
            }
        }
        uri?.let {
            viewModelScope.launch {
                val file = uriToImagePart(uri)
                val result =step1UseCase.postPhoto(
                    testId = testId,
                    photoType = step.value?:0,
                    file = file
                )

                result.onSuccess { testResponse ->
                    // 로그인 성공 시 실제 데이터 처리
                    testResponse?.let {

                        _postResult.postValue(it)
                    }

                }.onFailure { exception ->
                    // 로그인 정보 불러오기 실패

                }

            }
        }

    }

    suspend fun uriToImagePart(uri: Uri):MultipartBody. Part{
        // 이미지 압축 추가
        val compressedFile = compressImage(uri)

        // RequestBody 생성
        val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

        // MultipartBody.Part 형식으로 파일 데이터 준비
        val imagePart = MultipartBody.Part.createFormData("image", compressedFile.name, requestFile)

        return imagePart
    }

    // 이미지 압축 함수
    private suspend fun compressImage(imageUri: Uri): File {
        return withContext(Dispatchers.IO) {
            val contentResolver = getApplication<Application>().contentResolver

            // 비트맵으로 변환
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }

            // 원하는 크기로 리사이징 (선택 사항)
            val resizedBitmap = resizeBitmap(bitmap, maxWidth = 1024, maxHeight = 1024)

            // 압축을 위한 파일 생성
            val compressedFile = File(
                getApplication<Application>().cacheDir,
                "compressed_${System.currentTimeMillis()}.jpg"
            )

            // 압축 품질 설정 (0-100)
            val compressQuality = 70

            FileOutputStream(compressedFile).use { fos ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, fos)
                fos.flush()
            }

            // 원본 비트맵 리소스 해제
            bitmap.recycle()
            if (bitmap != resizedBitmap) {
                resizedBitmap.recycle()
            }

            compressedFile
        }
    }

    // 이미지 리사이징 함수
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun getPhotoStage():Int{
        return sharedPreferences.getInt("photoStage",0)
    }

    private fun savePhotoStage(step: Int) {
        sharedPreferences.edit().putInt("photoStage", step).apply()
    }

}
