package com.hnahofra.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Accès à la table Supabase "potholes" via l'API REST (PostgREST).
 * Données partagées par tous les utilisateurs.
 */
object PotholeRepository {

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private fun Request.Builder.withAuth(context: Context): Request.Builder {
        val key = SupabaseConfig.anonKey(context)
        return header("apikey", key)
            .header("Authorization", "Bearer $key")
    }

    /** Récupère tous les trous. Liste vide en cas d'erreur ou si non configuré. */
    suspend fun fetchAll(context: Context): List<Pothole> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) return@withContext emptyList()
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.restUrl(context)}?select=*")
                .withAuth(context)
                .get()
                .build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val body = resp.body?.string() ?: return@withContext emptyList()
                parseList(body)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Crée un nouveau signalement. @return true si réussi. */
    suspend fun add(context: Context, p: Pothole): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) return@withContext false
        try {
            val json = JSONObject()
                .put("reporter_name", p.reporterName)
                .put("state", p.state)
                .put("lat", p.lat)
                .put("lng", p.lng)
                .put("image_url", p.imageUrl)
                .put("date", p.dateMillis)
            val request = Request.Builder()
                .url(SupabaseConfig.restUrl(context))
                .withAuth(context)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .post(json.toString().toRequestBody(JSON))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    /** Marque un trou existant comme réparé (nouveau nom, photo et date). */
    suspend fun markRepaired(
        context: Context,
        id: String,
        reporterName: String,
        imageUrl: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured(context)) return@withContext false
        try {
            val json = JSONObject()
                .put("state", STATE_REPAIRED)
                .put("reporter_name", reporterName)
                .put("image_url", imageUrl)
                .put("date", System.currentTimeMillis())
            val request = Request.Builder()
                .url("${SupabaseConfig.restUrl(context)}?id=eq.$id")
                .withAuth(context)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .patch(json.toString().toRequestBody(JSON))
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Supprime un signalement. Réservé à l'admin : nécessite un jeton d'accès
     * (Supabase Auth) car la règle RLS n'autorise la suppression qu'aux
     * utilisateurs authentifiés.
     */
    suspend fun delete(context: Context, id: String): Boolean = withContext(Dispatchers.IO) {
        val token = AdminSession.token ?: return@withContext false
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.restUrl(context)}?id=eq.$id")
                .header("apikey", SupabaseConfig.anonKey(context))
                .header("Authorization", "Bearer $token")
                .header("Prefer", "return=minimal")
                .delete()
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    /** Change l'état d'un trou (utilisé par la modération admin). */
    suspend fun setState(context: Context, id: String, state: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().put("state", state)
                val request = Request.Builder()
                    .url("${SupabaseConfig.restUrl(context)}?id=eq.$id")
                    .withAuth(context)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .patch(json.toString().toRequestBody(JSON))
                    .build()
                client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: Exception) {
                false
            }
        }

    private fun parseList(body: String): List<Pothole> {
        val arr = JSONArray(body)
        val out = ArrayList<Pothole>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Pothole(
                    id = o.optString("id"),
                    reporterName = o.optString("reporter_name"),
                    state = o.optString("state", STATE_OPEN),
                    lat = o.optDouble("lat", 0.0),
                    lng = o.optDouble("lng", 0.0),
                    imageUrl = o.optString("image_url"),
                    dateMillis = o.optLong("date", System.currentTimeMillis())
                )
            )
        }
        return out
    }
}
