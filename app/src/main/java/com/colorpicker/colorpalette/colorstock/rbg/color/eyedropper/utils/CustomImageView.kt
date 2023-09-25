package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.utils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class CustomImageView : androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context) : super(context) {
        setBackgroundColor(0xFFFFFF)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)
        } catch (ignore: RuntimeException) {
        } catch (ignore: OutOfMemoryError) {
        }
    }

}