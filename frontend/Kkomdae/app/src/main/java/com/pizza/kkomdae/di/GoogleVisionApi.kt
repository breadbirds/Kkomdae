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
private val VISION_API_URL =
    "https://vision.googleapis.com/v1/images:annotate?key=${BuildConfig.VISION_API_KEY}"

object GoogleVisionApi {
    // Vision API 호출 함수
    fun callOcr(
        context: Context,
        base64Image: String,
        callback: (serial: String, barcode: String) -> Unit
    ) {
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
                try {
                    val responseBody = response.body?.string()
                    val fullText = try {
                        JSONObject(responseBody)
                            .getJSONArray("responses")
                            .getJSONObject(0)
                            .optJSONObject("fullTextAnnotation")
                            ?.getString("text") ?: ""
                    } catch (e: Exception) {
                        Log.e("VisionAPI", "JSON 파싱 오류: $responseBody")
                        ""
                    }

                    Log.d("VisionAPI", "인식된 전체 텍스트:\n$fullText")

                    val lines = fullText.split("\n")
                    var serial = ""
                    var barcode = ""

                    val serialRegex = Regex("^[A-Za-z0-9\\s#\\-_/\\\\]{6,}$")
                    val barcodeRegex = Regex("^A\\d{8,13}A?$")

                    // 📌 바코드 먼저 탐색
                    for (line in lines) {
                        val cleaned = line.replace(" ", "").trim()
                        if (barcodeRegex.matches(cleaned)) {
                            barcode = cleaned
                            break
                        }
                    }

                    // 📌 S/N 줄 인덱스
                    val snLineIndex = lines.indexOfFirst {
                        it.contains("S/N", ignoreCase = true) || it.contains("SN:", ignoreCase = true)
                    }

                    // 1️⃣ S/N 줄에서 추출
                    if (snLineIndex != -1) {
                        val snLine = lines[snLineIndex]
                        val parts = snLine.split(":", "：", "-")
                        for (part in parts) {
                            val cleanedPart = part.trim()
                            // 🔥 S/N 또는 SN 같은 키워드 제외
                            if (cleanedPart.equals("S/N", ignoreCase = true) || cleanedPart.equals("SN", ignoreCase = true)) continue

                            if (!cleanedPart.contains(Regex("[가-힣]")) &&
                                cleanedPart.length >= 4 &&
                                serialRegex.matches(cleanedPart)
                            ) {
                                serial = cleanedPart
                                break
                            }
                        }

                        // 2️⃣ S/N 다음 줄이 있으면 강력한 후보로 사용
                        if (serial.isEmpty() && snLineIndex + 1 < lines.size) {
                            val nextLine = lines[snLineIndex + 1].trim()
                            if (nextLine.isNotEmpty() &&
                                !nextLine.contains(Regex("[가-힣]")) &&
                                nextLine.length >= 5
                            ) {
                                serial = nextLine // 정규식 안 맞아도 사용
                            }
                        }
                    }

                    // 3️⃣ 그래도 없으면 전체 탐색
                    if (serial.isEmpty()) {
                        for (line in lines) {
                            val cleaned = line.trim()
                            val noSpace = cleaned.replace(" ", "")
                            if (!cleaned.contains(Regex("[가-힣]")) &&
                                cleaned.length >= 5 &&
                                !noSpace.startsWith("A") &&
                                !barcodeRegex.matches(noSpace) &&
                                cleaned != barcode
                            ) {
                                val filtered = cleaned.replace(Regex("[^A-Za-z0-9#\\-_/\\\\\\s]"), "")
                                if (filtered.contains(Regex("[A-Za-z]")) &&
                                    filtered.contains(Regex("[0-9]"))
                                ) {
                                    serial = filtered
                                    break
                                }
                            }
                        }
                    }

                    // 🎯 결과 콜백
                    (context as? android.app.Activity)?.runOnUiThread {
                        callback(serial, barcode)
                    }

                } catch (e: Exception) {
                    Log.e("VisionAPI", "전체 OCR 처리 중 에러: ${e.message}")
                    (context as? android.app.Activity)?.runOnUiThread {
                        callback("", "") // 앱이 죽지 않도록 빈 값 전달
                    }
                }
            }

        })
    }
}