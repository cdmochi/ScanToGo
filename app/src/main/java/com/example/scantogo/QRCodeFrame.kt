package com.example.scantogo

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.example.scantogo.extensions.toAlphaPercentage
import com.example.scantogo.extensions.toDP

@RequiresApi(Build.VERSION_CODES.Q)
class QRCodeFrame : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defStylesRes: Int) : super(
        context,
        attributes,
        defStylesRes
    )

    private val QR_HEIGHT_OFFSET = 120f.toDP()
    private val QR_WIDHT_OFFSET = 120f.toDP()

    private val qrFramePaint: Paint by lazy {
        Paint(ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            blendMode = BlendMode.XOR
        }
    }

    var backgroundLayerPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        alpha = 60.toAlphaPercentage()
    }
        set(value) {
            field = value
            invalidate()
        }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            it.drawRect(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(), backgroundLayerPaint
            )

            it.drawRoundRect(
                width / 2f - QR_WIDHT_OFFSET,
                QR_HEIGHT_OFFSET,
                width / 2f + QR_WIDHT_OFFSET,
                QR_HEIGHT_OFFSET * 3,
                20f.toDP(),
                20f.toDP(),
                qrFramePaint
            )
        }
    }
}