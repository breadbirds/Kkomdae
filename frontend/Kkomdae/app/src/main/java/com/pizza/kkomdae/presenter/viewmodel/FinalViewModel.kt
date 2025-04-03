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
import com.pizza.kkomdae.domain.model.FourthStageRequest
import com.pizza.kkomdae.domain.model.GetTotalResultResponse
import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.model.PostRePhotoResponse
import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.usecase.FinalUseCase

import dagger.hilt.android.internal.Contexts.getApplication
import com.pizza.kkomdae.domain.usecase.LoginUseCase
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

private const val TAG = "FinalViewModel"
@HiltViewModel
class FinalViewModel @Inject constructor(
    application: Application,
    private val finalUseCase: FinalUseCase
) :  AndroidViewModel(application) {

    private val _initFrontUri = MutableLiveData<String?>()
    val initFrontUri: LiveData<String?>
        get() = _initFrontUri

    private val _pdfName = MutableLiveData<String>()
    val pdfName: LiveData<String>
        get() = _pdfName

    private val _pdfUrl = MutableLiveData<String>()
    val pdfUrl: LiveData<String>
        get() = _pdfUrl

    private val _frontUri = MutableLiveData<String?>()
    val frontUri: LiveData<String?>
        get() = _frontUri

    private val _backUri = MutableLiveData<String?>()
    val backUri: LiveData<String?>
        get() = _backUri

    private val _leftUri = MutableLiveData<String?>()
    val leftUri: LiveData<String?>
        get() = _leftUri

    private val _rightUri = MutableLiveData<String?>()
    val rightUri: LiveData<String?>
        get() = _rightUri

    private val _screenUri = MutableLiveData<String?>()
    val screenUri: LiveData<String?>
        get() = _screenUri

    private val _keypadUri = MutableLiveData<String?>()
    val keypadUri: LiveData<String?>
        get() = _keypadUri

    private val _getFinalResult = MutableLiveData<GetTotalResultResponse>()
    val getFinalResult: LiveData<GetTotalResultResponse>
        get() = _getFinalResult

    private val _postFourth = MutableLiveData<PostResponse>()
    val postFourth: LiveData<PostResponse>
        get() = _postFourth

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _reCameraStage = MutableLiveData<Int>()
    val reCameraStage: LiveData<Int>
        get() = _reCameraStage

    private val _reCameraUri = MutableLiveData<Uri?>()
    val reCameraUri: LiveData<Uri?>
        get() = _reCameraUri

    // 전면부 재촬영 결과
    private val _rePhoto1 = MutableLiveData<PostRePhotoResponse?>()
    val rePhoto1: LiveData<PostRePhotoResponse?>
        get() = _rePhoto1

    // 후면부 재촬영 결과
    private val _rePhoto2 = MutableLiveData<PostRePhotoResponse?>()
    val rePhoto2: LiveData<PostRePhotoResponse?>
        get() = _rePhoto2

    // 좌측면 재촬영 결과
    private val _rePhoto3 = MutableLiveData<PostRePhotoResponse?>()
    val rePhoto3: LiveData<PostRePhotoResponse?>
        get() = _rePhoto3

    // 우측면 재촬영 결과
    private val _rePhoto4 = MutableLiveData<PostRePhotoResponse?>()
    val rePhoto4: LiveData<PostRePhotoResponse?>
        get() = _rePhoto4

    // 모니터 재촬영 결과
    private val _rePhoto5 = MutableLiveData<PostRePhotoResponse?>()
    val rePhoto5: LiveData<PostRePhotoResponse?>
        get() = _rePhoto5

    // 키보드 재촬영 결과
    private val _rePhoto6 = MutableLiveData<PostRePhotoResponse?>()
    val rePhoto6: LiveData<PostRePhotoResponse?>
        get() = _rePhoto6



    fun clearPostResponse(){
        _initFrontUri.postValue(null)
    }
    fun clearRePhoto1(){
        _rePhoto1.postValue(null)
    }
    fun clearRePhoto2(){
        _rePhoto2.postValue(null)
    }
    fun clearRePhoto3(){
        _rePhoto3.postValue(null)
    }
    fun clearRePhoto4(){
        _rePhoto4.postValue(null)
    }
    fun clearRePhoto5(){
        _rePhoto5.postValue(null)
    }
    fun clearRePhoto6(){
        _rePhoto6.postValue(null)
    }


    fun setReCameraUri(uri:Uri){
        _reCameraUri.postValue(uri)
    }

    fun setReCameraStage(stage:Int){
        _reCameraStage.postValue(stage)
    }

    fun postRePhoto(){
        val stage = reCameraStage.value?:1

        val loadingUrl = ""
        when(stage){
            1->{
                _frontUri.postValue(loadingUrl)
            }
            2->{

                _backUri.postValue(loadingUrl)
            }
            3->{

                _leftUri.postValue(loadingUrl)
            }
            4->{

                _rightUri.postValue(loadingUrl)
            }
            5->{

                _screenUri.postValue(loadingUrl)
            }
            6->{
                _keypadUri.postValue(loadingUrl)
            }
        }


        viewModelScope.launch {
            reCameraUri.value?.let {
                val file = uriToImagePart(it)
                _reCameraUri.postValue(null)
                try {
                    finalUseCase.postRePhoto(testId = sharedPreferences.getLong("test_id", 0), photoType = reCameraStage.value?:1, file = file).collect { response ->
                        Log.d(TAG, "postRePhoto: $response")

                        when(stage){
                            1->{
                                _rePhoto1.postValue(response)
                                _frontUri.postValue(response.data.photo_ai_url)
                            }
                            2->{
                                _rePhoto2.postValue(response)
                                _backUri.postValue(response.data.photo_ai_url)
                            }
                            3->{
                                _rePhoto3.postValue(response)
                                _leftUri.postValue(response.data.photo_ai_url)
                            }
                            4->{
                                _rePhoto4.postValue(response)
                                _rightUri.postValue(response.data.photo_ai_url)
                            }
                            5->{
                                _rePhoto5.postValue(response)
                                _screenUri.postValue(response.data.photo_ai_url)
                            }
                            6->{
                                _rePhoto6.postValue(response)
                                _keypadUri.postValue(response.data.photo_ai_url)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "postRePhoto 오류: ${e.message}", e)
                }

            }

        }
    }
    suspend fun uriToImagePart(uri: Uri): MultipartBody. Part{
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

    fun postPdf(){
        viewModelScope.launch {
            val result = finalUseCase.postPdf(sharedPreferences.getLong("test_id", 0))
            Log.d(TAG, "postPdf: $result")
            result.onSuccess {
                _pdfName.postValue(it.message)
            }
        }
    }

    fun getLaptopTotalResult(){
        viewModelScope.launch {
            val result = finalUseCase.getLaptopTotalResult(sharedPreferences.getLong("test_id", 0))
            Log.d(TAG, "getLaptopTotalResult: $result")
            result.onSuccess {
                _getFinalResult.postValue(it)
            }
        }
    }

    // 비고 입력
    fun postFourthStage(description:String){
        viewModelScope.launch {
            val data = FourthStageRequest(
                testId = sharedPreferences.getLong("test_id", 0),
                description = description
            )
            val result = finalUseCase.postFourthStage(data)
            Log.d(TAG, "postFourthStage: $result")
            result.onSuccess {
                _postFourth.postValue(it)
            }
        }

    }


    fun getAiPhoto(){

        viewModelScope.launch {
            val result = finalUseCase.getAiPhoto(sharedPreferences.getLong("test_id", 0))

            Log.d(TAG, "getAiPhoto: $result")
            result.onSuccess {
                _initFrontUri.postValue(it.data.Picture1_ai_url)
                _frontUri.postValue(it.data.Picture1_ai_url)
                _backUri.postValue(it.data.Picture2_ai_url)
                _leftUri.postValue(it.data.Picture3_ai_url)
                _rightUri.postValue(it.data.Picture4_ai_url)
                _screenUri.postValue(it.data.Picture5_ai_url)
                _keypadUri.postValue(it.data.Picture6_ai_url)
            }


        }
    }

    fun getPdfUrl(name: String){
        viewModelScope.launch {
            val result = finalUseCase.getPdfUrl(name)
            Log.d(TAG, "getPdfUrl: $result")
            result.onSuccess {
                _pdfUrl.postValue(it.url)
            }
        }
    }




}