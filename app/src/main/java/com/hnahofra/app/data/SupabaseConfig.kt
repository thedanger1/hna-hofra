package com.hnahofra.app.data

import android.content.Context
import com.hnahofra.app.R

/**
 * Paramètres de connexion Supabase, lus depuis res/values/secrets.xml.
 * On utilise directement l'API REST (PostgREST) : pas de SDK lourd.
 */
object SupabaseConfig {

    /**
     * URL de base du projet (sans chemin). On tolère que l'utilisateur ait
     * collé l'URL avec un suffixe "/rest/v1" : on le retire pour éviter un
     * doublon de chemin.
     */
    fun url(context: Context): String {
        var u = context.getString(R.string.supabase_url).trim().trimEnd('/')
        if (u.endsWith("/rest/v1")) u = u.removeSuffix("/rest/v1")
        return u.trimEnd('/')
    }

    fun anonKey(context: Context): String =
        context.getString(R.string.supabase_anon_key)

    fun isConfigured(context: Context): Boolean {
        val u = context.getString(R.string.supabase_url)
        val k = context.getString(R.string.supabase_anon_key)
        return !u.startsWith("METTRE") && !k.startsWith("METTRE") &&
            u.isNotBlank() && k.isNotBlank()
    }

    /** Point d'accès REST de la table "potholes". */
    fun restUrl(context: Context): String = "${url(context)}/rest/v1/potholes"
}
