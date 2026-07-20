package com.hnahofra.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Session administrateur en mémoire. Contient le jeton d'accès Supabase Auth
 * du créateur (obtenu après connexion). Tant qu'il est présent, l'app affiche
 * les actions de modération (supprimer / modifier).
 *
 * Volontairement non persistée : à chaque redémarrage, l'admin se reconnecte.
 */
object AdminSession {
    var token by mutableStateOf<String?>(null)

    val isActive: Boolean get() = token != null

    fun clear() {
        token = null
    }
}
