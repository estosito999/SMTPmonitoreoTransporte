package com.example.smtpmonitoreo.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable

/** Texto corto para el marcador: "102" → "102", "Trufi 45" → "45". */
internal fun lineLabel(line: String): String =
    line.filter { it.isDigit() }.ifEmpty { line.take(3) }.take(4).ifBlank { "•" }

internal object MarkerIcons {
    private val cache = HashMap<String, BitmapDrawable>()

    fun get(context: Context, label: String, color: Int, selected: Boolean): BitmapDrawable {
        val key = "$label|$color|$selected"
        return cache.getOrPut(key) { draw(context.applicationContext, label, color, selected) }
    }

    private fun draw(context: Context, label: String, color: Int, selected: Boolean): BitmapDrawable {
        val density = context.resources.displayMetrics.density
        val size = ((if (selected) 50 else 40) * density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val c = size / 2f

        // Halo
        paint.color = color
        paint.alpha = 70
        canvas.drawCircle(c, c, c, paint)

        // Anillo de color
        paint.alpha = 255
        canvas.drawCircle(c, c, c * 0.78f, paint)

        // Centro oscuro
        paint.color = 0xFF0B0F19.toInt()
        canvas.drawCircle(c, c, c * 0.60f, paint)

        // Anillo blanco si está seleccionada
        if (selected) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f * density
            paint.color = Color.WHITE
            canvas.drawCircle(c, c, c * 0.88f, paint)
            paint.style = Paint.Style.FILL
        }

        // Texto
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = size * if (label.length > 3) 0.24f else 0.30f
        val textY = c - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(label, c, textY, paint)

        return BitmapDrawable(context.resources, bitmap)
    }
}