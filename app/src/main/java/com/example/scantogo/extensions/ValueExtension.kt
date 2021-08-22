package com.example.scantogo.extensions

import android.content.res.Resources
import android.util.TypedValue

fun Float.toDP() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

fun Int.toDP() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

fun Int.toAlphaPercentage() = (255 * (this / 100f)).toInt()