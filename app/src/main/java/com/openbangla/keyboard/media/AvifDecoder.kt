package com.openbangla.keyboard.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.InputStream

object AvifDecoder {

    fun isAvifSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    fun decodeStream(inputStream: InputStream): Bitmap? {
        return try {
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeByteArray(data: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(data, 0, data.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
