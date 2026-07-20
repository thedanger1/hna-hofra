package com.hnahofra.app.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

const val STATE_OPEN = "OPEN"          // nid-de-poule non réparé
const val STATE_REPAIRED = "REPAIRED"  // réparé

/**
 * Un signalement de trou. Un seul document par trou ; il est mis à jour
 * lorsqu'un utilisateur le déclare réparé.
 *
 * [date] = date de la dernière photo :
 *   - si OPEN  -> date de découverte (dernier déclarant)
 *   - si REPAIRED -> date de la réparation (dernier déclarant)
 */
data class Pothole(
    @DocumentId val id: String = "",
    val reporterName: String = "",
    val state: String = STATE_OPEN,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val imageUrl: String = "",
    val date: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now()
) {
    @get:Exclude
    val isRepaired: Boolean get() = state == STATE_REPAIRED
}
