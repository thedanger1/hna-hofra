package com.hnahofra.app.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.hnahofra.app.R

/**
 * Initialise Firebase SANS le plugin google-services : les 3 valeurs
 * (projectId, appId, apiKey) sont lues depuis res/values/secrets.xml.
 * Cela permet à l'app de compiler même si les clés ne sont pas encore
 * renseignées (le build cloud n'échoue pas).
 */
object FirebaseInit {
    @Volatile private var initialized = false

    fun isConfigured(context: Context): Boolean {
        val projectId = context.getString(R.string.firebase_project_id)
        val appId = context.getString(R.string.firebase_app_id)
        val apiKey = context.getString(R.string.firebase_api_key)
        return !projectId.startsWith("METTRE") &&
            !appId.startsWith("METTRE") &&
            !apiKey.startsWith("METTRE") &&
            projectId.isNotBlank() && appId.isNotBlank() && apiKey.isNotBlank()
    }

    /** @return true si Firebase est prêt à l'emploi. */
    fun ensure(context: Context): Boolean {
        if (initialized) return true
        if (!isConfigured(context)) return false
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setProjectId(context.getString(R.string.firebase_project_id))
                .setApplicationId(context.getString(R.string.firebase_app_id))
                .setApiKey(context.getString(R.string.firebase_api_key))
                .build()
            FirebaseApp.initializeApp(context, options)
        }
        initialized = true
        return true
    }
}
