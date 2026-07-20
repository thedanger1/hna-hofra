package com.hnahofra.app.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Ville de Safi (Maroc). La zone autorisée correspond au **tracé administratif
 * officiel de la commune de Safi** (le contour affiché par Google Maps).
 *
 * Le polygone [BOUNDARY] provient d'OpenStreetMap (relation administrative
 * "Safi آسفي", simplifiée à ~80 m). Il sert à :
 *   - dessiner la frontière (contour rouge pointillé) sur la carte ;
 *   - valider qu'un signalement est bien à l'intérieur de la ville
 *     ([contains] = test point-dans-polygone, pas un simple rectangle).
 */
object Safi {

    /** Contour de la commune de Safi (fermé : dernier point = premier point). */
    val BOUNDARY: List<LatLng> = listOf(
        LatLng(32.3275711, -9.274596),
        LatLng(32.320429, -9.2598773),
        LatLng(32.3200438, -9.2545519),
        LatLng(32.3155675, -9.2515899),
        LatLng(32.3128539, -9.2517406),
        LatLng(32.312807, -9.2493055),
        LatLng(32.3098055, -9.2470514),
        LatLng(32.309111, -9.2490998),
        LatLng(32.3093827, -9.2464973),
        LatLng(32.305415, -9.2458718),
        LatLng(32.3063347, -9.2485202),
        LatLng(32.3052565, -9.2483995),
        LatLng(32.3021037, -9.2445473),
        LatLng(32.3016888, -9.2455946),
        LatLng(32.3051831, -9.2494535),
        LatLng(32.3115003, -9.253031),
        LatLng(32.3134974, -9.2562027),
        LatLng(32.313196, -9.2567177),
        LatLng(32.3113757, -9.2534173),
        LatLng(32.3046609, -9.2495549),
        LatLng(32.3019381, -9.2464663),
        LatLng(32.3003432, -9.2437821),
        LatLng(32.2859354, -9.2470604),
        LatLng(32.275409, -9.2521164),
        LatLng(32.2729733, -9.2520869),
        LatLng(32.2579113, -9.2601442),
        LatLng(32.2512221, -9.2657501),
        LatLng(32.2457778, -9.2647684),
        LatLng(32.2422524, -9.2598277),
        LatLng(32.2396159, -9.2608415),
        LatLng(32.2346205, -9.2580706),
        LatLng(32.2325702, -9.2549852),
        LatLng(32.2186082, -9.2509072),
        LatLng(32.2079309, -9.2538491),
        LatLng(32.203233, -9.2522746),
        LatLng(32.2001735, -9.2527843),
        LatLng(32.1915598, -9.2570999),
        LatLng(32.1904319, -9.24152),
        LatLng(32.1920354, -9.2382863),
        LatLng(32.1897782, -9.2304049),
        LatLng(32.2017989, -9.2296324),
        LatLng(32.2096424, -9.2221652),
        LatLng(32.2175269, -9.2111638),
        LatLng(32.229907, -9.2165712),
        LatLng(32.2515046, -9.2190603),
        LatLng(32.2604325, -9.2112497),
        LatLng(32.2714224, -9.1937123),
        LatLng(32.2830332, -9.1931544),
        LatLng(32.2885116, -9.1910086),
        LatLng(32.308862, -9.1878758),
        LatLng(32.3246077, -9.1895345),
        LatLng(32.3244209, -9.1992484),
        LatLng(32.3178568, -9.2122088),
        LatLng(32.3333779, -9.2135821),
        LatLng(32.3380555, -9.2575682),
        LatLng(32.3400579, -9.2657083),
        LatLng(32.336131, -9.2708281),
        LatLng(32.3275711, -9.274596)
    )

    /** Rectangle englobant (le SDK Maps ne borne la caméra que par un rectangle). */
    val BOUNDS: LatLngBounds = LatLngBounds.builder()
        .apply { BOUNDARY.forEach { include(it) } }
        .build()

    val CENTER: LatLng = BOUNDS.center

    const val MIN_ZOOM = 11f
    const val DEFAULT_ZOOM = 12f

    /** Test point-dans-polygone (lancer de rayon) sur le tracé réel de Safi. */
    fun contains(lat: Double, lng: Double): Boolean {
        val pts = BOUNDARY
        var inside = false
        var j = pts.size - 1
        for (i in pts.indices) {
            val yi = pts[i].latitude
            val xi = pts[i].longitude
            val yj = pts[j].latitude
            val xj = pts[j].longitude
            if (((yi > lat) != (yj > lat)) &&
                (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
