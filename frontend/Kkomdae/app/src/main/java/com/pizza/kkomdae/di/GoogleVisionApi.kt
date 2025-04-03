package com.pizza.kkomdae.di

import android.content.Context
import android.util.Log
import com.pizza.kkomdae.BuildConfig

import com.pizza.kkomdae.base.ApplicationClass
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

// 🔐 Google Cloud Vision API 키 (노출 주의!)
private val VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate?key=${BuildConfig.VISION_API_KEY}"

object GoogleVisionApi {
    // Vision API 호출 함수
    fun callOcr(context: Context, base64Image: String, callback: (serial: String, barcode: String) -> Unit) {
        val jsonRequest = JSONObject().apply {
            put("requests", JSONArray().put(
                JSONObject().apply {
                    put("image", JSONObject().put("content", base64Image))
                    put(
                        "features", JSONArray().put(
                            JSONObject().put("type", "TEXT_DETECTION")
                        )
                    )
                }
            ))
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            jsonRequest.toString()
        )

        val request = Request.Builder()
            .url(VISION_API_URL)
            .post(requestBody)
            .build()

        val client = ApplicationClass.retrofit.callFactory() as OkHttpClient

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("VisionAPI", "API 호출 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val fullText = try {
                    JSONObject(responseBody)
                        .getJSONArray("responses")
                        .getJSONObject(0)
                        .optJSONObject("fullTextAnnotation")
                        ?.getString("text") ?: ""
                } catch (e: Exception) {
                    Log.e("VisionAPI", "파싱 오류: $responseBody")
                    ""
                }

                // 🔍 여기서 fullText 로그 찍기!
                Log.d("VisionAPI", "인식된 전체 텍스트:\n$fullText")

                val lines = fullText.split("\n")
                var serial = ""
                var barcode = ""

                for (i in lines.indices) {
                    val line = lines[i].trim()

                    // S/N 다음 줄이 시리얼일 가능성
                    if (line.contains("S/N") && i + 1 < lines.size) {
                        serial = lines[i + 1].trim()
                    }

                    // 바코드 패턴: A로 시작해서 숫자 8~13개, A로 끝날 수도 있음 (공백 제거 후 비교)
                    val cleaned = line.replace(" ", "")
                    if (Regex("A\\d{8,13}A?").matches(cleaned)) {
                        barcode = cleaned
                    }
                }



                (context as? android.app.Activity)?.runOnUiThread {
                    callback(serial, barcode)
                }
            }
        })
    }
}