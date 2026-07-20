package com.hnahofra.app.data

const val STATE_OPEN = "OPEN"          // nid-de-poule non réparé
const val STATE_REPAIRED = "REPAIRED"  // réparé

/**
 * Un signalement de trou. Un seul enregistrement par trou ; il est mis à jour
 * lorsqu'un utilisateur le déclare réparé.
 *
 * [dateMillis] = date de la dernière photo (epoch en millisecondes) :
 *   - si OPEN     -> date de découverte (dernier déclarant)
 *   - si REPAIRED -> date de la réparation (dernier déclarant)
 */
data class Pothole(
    val id: String = "",
    val reporterName: String = "",
    val state: String = STATE_OPEN,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val imageUrl: String = "",
    val dateMillis: Long = System.currentTimeMillis()
) {
    val isRepaired: Boolean get() = state == STATE_REPAIRED
}
