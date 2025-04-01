package com.pizza.kkomdae.util

import android.graphics.RectF

data class BBox(val rect: RectF, val label: String, val confidence: Float)

data class PreprocessedResult(
    val input: Array<Array<Array<FloatArray>>>,
    val scale: Float,
    val dx: Float,
    val dy: Float
)

class AutoCapture {
}