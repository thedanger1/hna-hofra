package com.hnahofra.app.data

import android.content.Context
import com.hnahofra.app.R

/**
 * Paramètres de connexion Supabase, lus depuis res/values/secrets.xml.
 * On utilise directement l'API REST (PostgREST) : pas de SDK lourd.
 */
object SupabaseConfig {

    fun url(context: Context): String =
        context.getString(R.string.supabase_url).trimEnd('/')

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
