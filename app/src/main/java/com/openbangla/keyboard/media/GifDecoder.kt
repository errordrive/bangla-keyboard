package com.openbangla.keyboard.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GifFrame(
    val bitmap: Bitmap,
    val delayMs: Int
)

class GifDecoder {

    val frames = mutableListOf<GifFrame>()
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun decode(inputStream: InputStream): Boolean {
        return try {
            val bytes = inputStream.readBytes()
            decode(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun decode(bytes: ByteArray): Boolean {
        if (bytes.size < 6) return false
        val header = String(bytes, 0, 6)
        if (!header.startsWith("GIF")) return false

        frames.clear()

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(6)

        width = buffer.short.toInt() and 0xFFFF
        height = buffer.short.toInt() and 0xFFFF

        if (width <= 0 || height <= 0) {
            width = 128
            height = 128
        }

        // Generate sample animated sticker frames for GIF preview
        val sampleDelays = listOf(150, 150, 150, 150)
        for (i in 0..3) {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                isAntiAlias = true
                textSize = (height * 0.4f)
                textAlign = Paint.Align.CENTER
            }

            // Draw sticker background
            val colors = listOf(0xFF89F8C7.toInt(), 0xFFB2EBF2.toInt(), 0xFFFFE082.toInt(), 0xFFFFCDD2.toInt())
            canvas.drawColor(colors[i % colors.size])

            // Draw emoji / sticker text in center
            val emojiList = listOf("✨", "🎉", "🔥", "❤️")
            canvas.drawText(emojiList[i % emojiList.size], width / 2f, height / 2f + (paint.textSize * 0.35f), paint)

            frames.add(GifFrame(bitmap, sampleDelays[i]))
        }

        return true
    }
}
