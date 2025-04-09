package com.pizza.kkomdae.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class QrScanOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#80000000") // 검은색 50% 투명 (#80 = 50% 투명도)
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val isLandscape = width > height  // ✅ 가로 모드인지 체크

        val overlayMargin = if (isLandscape) width * 0.2f else height * 0.12f  // ✅ 네모 틀 마진 조정

        // 16:9 비율로 크기 설정
        val rectWidth = width - overlayMargin * 2
        val rectHeight = rectWidth / 20f * 20f  // 16:9 비율 유지

        val left = (width - rectWidth) / 2
        val top = (height - rectHeight) / 2
        val right = left + rectWidth
        val bottom = top + rectHeight

        rect.set(left, top, right, bottom)

        // 🔹 1️⃣ 전체 반투명 배경을 먼저 채운다
        val path = Path().apply {
            addRoundRect(rect, 50f, 50f, Path.Direction.CW) // ✅ 둥근 모서리 적용된 네모 틀
        }

        val overlayPath = Path().apply {
            addRect(0f, 0f, width, height, Path.Direction.CW)
            op(path, Path.Op.DIFFERENCE) // ✅ 네모 틀 부분을 깎아서 투명하게 만듦
        }

        canvas.drawPath(overlayPath, overlayPaint)

        // 🔹 2️⃣ 네모 테두리를 그린다
        canvas.drawRoundRect(rect, 50f, 50f, borderPaint)
    }
}
