package com.hnahofra.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.hnahofra.app.R
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Envoi des photos vers imgbb (offre gratuite). Conservation 6 mois. */
object ImgbbUploader {

    // 6 mois ≈ 180 jours (maximum autorisé par imgbb), en secondes.
    private const val EXPIRATION_SECONDS = 15_552_000L
    private const val MAX_DIMENSION = 1280

    private val client = OkHttpClient()

    fun isConfigured(context: Context): Boolean =
        !context.getString(R.string.imgbb_api_key).startsWith("METTRE")

    /** @return l'URL publique de l'image, ou null en cas d'échec. */
    suspend fun upload(context: Context, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val key = context.getString(R.string.imgbb_api_key)
            if (key.startsWith("METTRE")) return@withContext null

            val base64 = readAsCompressedBase64(context, imageUri) ?: return@withContext null

            val url = "https://api.imgbb.com/1/upload" +
                "?expiration=$EXPIRATION_SECONDS&key=$key"
            val body = FormBody.Builder().add("image", base64).build()
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val json = JSONObject(resp.body?.string() ?: return@withContext null)
                if (!json.optBoolean("success")) return@withContext null
                val data = json.getJSONObject("data")
                data.optString("display_url").ifBlank { data.optString("url") }
                    .ifBlank { null }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun readAsCompressedBase64(context: Context, uri: Uri): String? {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return null

        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        bmp = applyExifRotation(bytes, bmp)
        bmp = scaleDown(bmp, MAX_DIMENSION)

        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 72, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    private fun scaleDown(bmp: Bitmap, max: Int): Bitmap {
        val w = bmp.width; val h = bmp.height
        if (w <= max && h <= max) return bmp
        val ratio = minOf(max.toFloat() / w, max.toFloat() / h)
        return Bitmap.createScaledBitmap(bmp, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }

    private fun applyExifRotation(bytes: ByteArray, bmp: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(bytes.inputStream())
            val degrees = when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degrees == 0f) bmp
            else Bitmap.createBitmap(
                bmp, 0, 0, bmp.width, bmp.height,
                Matrix().apply { postRotate(degrees) }, true
            )
        } catch (e: Exception) {
            bmp
        }
    }
}
