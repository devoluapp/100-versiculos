package blog.robertotavares.cemversiculos.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    
    fun shareText(context: Context, text: String, title: String = "Compartilhar") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        
        try {
            val stream = FileOutputStream("$cachePath/image.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
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
}
