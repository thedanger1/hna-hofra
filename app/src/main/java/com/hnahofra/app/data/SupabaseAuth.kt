package com.hnahofra.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Connexion administrateur via Supabase Auth (email + mot de passe). */
object SupabaseAuth {

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    /** @return true si la connexion réussit ; stocke le jeton dans [AdminSession]. */
    suspend fun signIn(context: Context, email: String, password: String): Boolean =
        withContext(Dispatchers.IO) {
            if (!SupabaseConfig.isConfigured(context)) return@withContext false
            try {
                val body = JSONObject()
                    .put("email", email)
                    .put("password", password)
                    .toString()
                    .toRequestBody(JSON)
                val request = Request.Builder()
                    .url("${SupabaseConfig.url(context)}/auth/v1/token?grant_type=password")
                    .header("apikey", SupabaseConfig.anonKey(context))
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()
                client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext false
                    val json = JSONObject(resp.body?.string() ?: return@withContext false)
                    val token = json.optString("access_token")
                    if (token.isBlank()) return@withContext false
                    AdminSession.token = token
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
}
