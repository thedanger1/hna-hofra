package com.hnahofra.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/** Convertit un drawable vectoriel en icône de marqueur Google Maps. */
object MapIcons {
    fun fromVector(context: Context, resId: Int, sizePx: Int = 96): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, resId)!!
        drawable.setBounds(0, 0, sizePx, sizePx)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        drawable.draw(Canvas(bitmap))
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
