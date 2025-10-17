package com.back.chap.components.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources

/**
 * 地図ピンアイコンを生成する関数（画像の形状に準拠）
 * @param color ピンの色
 * @return ビットマップ画像
 */
fun bitmapFromVector(context: Context, drawableResId: Int): Bitmap {
    val drawable: Drawable = requireNotNull(AppCompatResources.getDrawable(context, drawableResId))
    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}