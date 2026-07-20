package com.hnahofra.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Gère la langue de l'application (fr / ar) et la persiste.
 * L'app est mono-Activité : on change la langue puis on recrée l'Activité.
 */
object LocaleManager {
    private const val PREFS = "settings"
    private const val KEY_LANG = "lang"
    const val FR = "fr"
    const val AR = "ar"

    fun currentLang(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, FR) ?: FR

    fun setLang(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
    }

    fun toggle(context: Context): String {
        val next = if (currentLang(context) == FR) AR else FR
        setLang(context, next)
        return next
    }

    /** Enveloppe le contexte avec la langue choisie (à appeler dans attachBaseContext). */
    fun wrap(context: Context): Context {
        val locale = Locale(currentLang(context))
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
