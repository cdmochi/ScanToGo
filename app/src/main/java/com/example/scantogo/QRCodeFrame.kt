package com.example.scantogo

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.example.scantogo.extensions.toAlphaPercentage
import com.example.scantogo.extensions.toDP

@RequiresApi(Build.VERSION_CODES.Q)
class QRCodeFrame: View {
    constructor(context: Context): super(context)
    constructor(context: Context, attributes: AttributeSet): super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defStylesRes: Int): super(context, attributes, defStylesRes)

    private val QR_HEIGHT_OFFSET = 120f.toDP()
    private val QR_WIDHT_OFFSET = 120f.toDP()

    private val qrFramePaint: Paint by lazy {
        Paint(ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            blendMode = BlendMode.XOR
        }
    }

    private val backgroundLayerPaint: Paint by lazy {
        Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = 60.toAlphaPercentage()
        }
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
                20.toDP(),
                20f.toDP(),
                qrFramePaint
            )
        }
    }
}