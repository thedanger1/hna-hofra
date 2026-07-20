package com.hnahofra.app.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Accès à la collection Firestore "potholes" (données partagées par tous). */
object PotholeRepository {

    private const val COLLECTION = "potholes"

    private fun col(context: Context) = run {
        FirebaseInit.ensure(context)
        FirebaseFirestore.getInstance().collection(COLLECTION)
    }

    /**
     * Écoute en temps réel tous les trous. Retourne un enregistrement à retirer
     * (onDispose). Null si Firebase n'est pas configuré.
     */
    fun listen(context: Context, onData: (List<Pothole>) -> Unit): ListenerRegistration? {
        if (!FirebaseInit.ensure(context)) return null
        return col(context).addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            onData(snap.documents.mapNotNull { it.toObject(Pothole::class.java) })
        }
    }

    /** Crée un nouveau signalement. @return true si réussi. */
    suspend fun add(context: Context, p: Pothole): Boolean = try {
        col(context).add(p.copy(id = "")).awaitResult()
        true
    } catch (e: Exception) {
        false
    }

    /** Marque un trou existant comme réparé (nouveau nom, photo et date). */
    suspend fun markRepaired(
        context: Context,
        id: String,
        reporterName: String,
        imageUrl: String
    ): Boolean = try {
        col(context).document(id).update(
            mapOf(
                "state" to STATE_REPAIRED,
                "reporterName" to reporterName,
                "imageUrl" to imageUrl,
                "date" to Timestamp.now()
            )
        ).awaitResult()
        true
    } catch (e: Exception) {
        false
    }
}

/** Convertit un Task Google en fonction suspendue. */
suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { if (cont.isActive) cont.resume(it) }
    addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
}
