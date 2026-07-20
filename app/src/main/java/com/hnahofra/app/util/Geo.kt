package com.hnahofra.app.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/** Zone de la ville de Safi (Maroc). La carte est verrouillée sur cette zone. */
object Safi {
    val CENTER = LatLng(32.2994, -9.2372)

    // Sud-Ouest et Nord-Est délimitant l'agglomération de Safi.
    val BOUNDS = LatLngBounds(
        LatLng(32.2400, -9.3050), // SW
        LatLng(32.3600, -9.1750)  // NE
    )

    const val MIN_ZOOM = 12f
    const val DEFAULT_ZOOM = 13.5f

    fun contains(lat: Double, lng: Double): Boolean =
        BOUNDS.contains(LatLng(lat, lng))
}
