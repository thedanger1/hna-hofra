package com.hnahofra.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/** Convertit un drawable vectoriel en icône de marqueur Google Maps. */
object MapIcons {
    /** @return l'icône, ou null pour utiliser le marqueur par défaut en secours. */
    fun fromVector(context: Context, resId: Int, sizePx: Int = 96): BitmapDescriptor? {
        return try {
            val drawable = ContextCompat.getDrawable(context, resId) ?: return null
            drawable.setBounds(0, 0, sizePx, sizePx)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            drawable.draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            null
        }
    }
}
