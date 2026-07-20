package com.hnahofra.app.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Zone de la ville de Safi (Maroc). La carte est verrouillée sur cette zone.
 *
 * Limites définies à partir des repères fournis :
 *   - Sud        : Office Chérifien des Phosphates (OCP)      -> latitude min
 *   - Est        : CMPE (Cie Marocaine de Plâtre)             -> longitude max
 *   - Nord-Est   : La Digue de Safi / Forêt Sidi M'Sahel      -> latitude max
 *   - Nord-Ouest : côte Ayt Turkin                            -> longitude min
 *
 * Ces valeurs sont des estimations serrées. Pour les régler au mètre près :
 * dans Google Maps, appui long sur le lieu -> "copier les coordonnées", puis
 * ajustez les 4 nombres ci-dessous.
 */
object Safi {
    // Coin Sud-Ouest (latitude min = OCP au sud ; longitude min = côte au NO)
    private val SOUTH_WEST = LatLng(32.2350, -9.2650)
    // Coin Nord-Est (latitude max = Forêt Sidi M'Sahel / La Digue ; longitude max = CMPE à l'est)
    private val NORTH_EAST = LatLng(32.3450, -9.1850)

    val BOUNDS = LatLngBounds(SOUTH_WEST, NORTH_EAST)
    val CENTER: LatLng = BOUNDS.center

    const val MIN_ZOOM = 12.5f
    const val DEFAULT_ZOOM = 13.5f

    fun contains(lat: Double, lng: Double): Boolean =
        BOUNDS.contains(LatLng(lat, lng))
}
