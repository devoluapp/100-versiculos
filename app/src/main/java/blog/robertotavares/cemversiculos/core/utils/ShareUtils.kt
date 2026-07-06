package blog.robertotavares.cemversiculos.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import blog.robertotavares.cemversiculos.R
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareText(context: Context, text: String, title: String = "Compartilhar") {
        val playStoreLink = "https://play.google.com/store/apps/details?id=${context.packageName}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$text\n\n$playStoreLink")
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareBitmap(context: Context, bitmap: Bitmap, isPremium: Boolean) {
        val finalBitmap = if (isPremium) bitmap else addWatermark(context, bitmap)

        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()

        try {
            val stream = FileOutputStream("$cachePath/image.png")
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val imagePath = File(context.cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            newFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Imagem"))
    }

    private fun addWatermark(context: Context, bitmap: Bitmap): Bitmap {
        val watermarked = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(watermarked)
        val density = context.resources.displayMetrics.density

        val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 150, 150, 150)
            textSize = 13 * density
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val messagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 150, 150, 150)
            textSize = 11 * density
            textAlign = Paint.Align.CENTER
        }

        val centerX = watermarked.width / 2f
        val bottomPadding = 14 * density
        val messageY = watermarked.height - bottomPadding
        val appNameY = messageY - messagePaint.textSize - (4 * density)

        canvas.drawText(context.getString(R.string.app_name), centerX, appNameY, appNamePaint)
        canvas.drawText(context.getString(R.string.watermark_download_free), centerX, messageY, messagePaint)

        return watermarked
    }
}
