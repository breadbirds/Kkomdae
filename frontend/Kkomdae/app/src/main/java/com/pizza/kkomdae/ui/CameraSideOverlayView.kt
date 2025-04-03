package com.pizza.kkomdae.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.pizza.kkomdae.util.BBox

class CameraSideOverlayView @JvmOverloads constructor(
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

    private val guideRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val isLandscape = width > height  // ✅ 가로 모드인지 체크

        val overlayMargin = if (isLandscape) width * 0.18f else height * 0.18f  // ✅ 네모 틀 마진 조정

        // 16:9 비율로 크기 설정
        val rectWidth = width - overlayMargin * 2
        val rectHeight = rectWidth / 229.1f * 30f  // 16:9 비율 유지

        val left = (width - rectWidth) / 2
        val top = (height - rectHeight) / 2
        val right = left + rectWidth
        val bottom = top + rectHeight

        guideRect.set(left, top, right, bottom)

        // 🔹 1️⃣ 전체 반투명 배경을 먼저 채운다
        val path = Path().apply {
            addRoundRect(guideRect, 30f, 30f, Path.Direction.CW) // ✅ 둥근 모서리 적용된 네모 틀
        }

        val overlayPath = Path().apply {
            addRect(0f, 0f, width, height, Path.Direction.CW)
            op(path, Path.Op.DIFFERENCE) // ✅ 네모 틀 부분을 깎아서 투명하게 만듦
        }

        canvas.drawPath(overlayPath, overlayPaint)

        // 🔹 2️⃣ 네모 테두리를 그린다
        canvas.drawRoundRect(guideRect, 30f, 30f, borderPaint)

        // 감지된 bbox그리기
        for (box in boxes) {
            canvas.drawRect(box.rect, boxPaint)
            canvas.drawText("${box.label} ${(box.confidence * 100).toInt()}%", box.rect.left, box.rect.top - 10, textPaint)
        }
    }

    // ✅ OverlayView에 guideRect getter 추가
    fun getGuideRect(): RectF? = guideRect

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textPaint = Paint().apply {
        color = Color.GREEN
        textSize = 50f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val boxes = mutableListOf<BBox>()

    fun setBoxes(newBoxes: List<BBox>, bitmapWidth: Int, bitmapHeight: Int) {
        boxes.clear()

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val ratioBitmap = bitmapWidth / bitmapHeight.toFloat()
        val ratioView = viewWidth / viewHeight.toFloat()

        val scale: Float
        val dx: Float
        val dy: Float

        if (ratioView > ratioBitmap) {
            // 좌우 딱 맞음, 위아래 여백
            scale = viewWidth / bitmapWidth
            dx = 0f
            dy = (viewHeight - bitmapHeight * scale) / 2f
        } else {
            // 위아래 딱 맞음, 좌우 여백
            scale = viewHeight / bitmapHeight
            dx = (viewWidth - bitmapWidth * scale) / 2f
            dy = 0f
        }

        // 스케일링 + 여백 보정
        val scaledBoxes = newBoxes.map { box ->
            val rect = box.rect
            val scaledRect = RectF(
                rect.left * scale + dx,
                rect.top * scale + dy,
                rect.right * scale + dx,
                rect.bottom * scale + dy
            )
            BBox(scaledRect, box.label, box.confidence)
        }

        boxes.addAll(scaledBoxes)
        invalidate()
    }
}
