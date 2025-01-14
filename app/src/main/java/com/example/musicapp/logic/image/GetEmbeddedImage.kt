package com.example.musicapp.logic.image

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.documentfile.provider.DocumentFile
import com.example.musicapp.logic.album.Album

fun getEmbeddedImage(
    album: Album,
    context: Context
): ImageBitmap? {
    val documentFile = DocumentFile.fromTreeUri(context, album.uri)

    if (documentFile == null || !documentFile.isDirectory) return null

    val supportedAudioFormats = listOf(
        "mp3", "flac", "m4a", "aac", "wav", "ogg",
        "amr", "mid", "xmf", "mxmf", "rtttl", "rtx",
        "ota", "imy", "3gp", "ts", "mkv", "mpeg"
    )

    val files = documentFile.listFiles()

    val retriever = MediaMetadataRetriever()
    try {
        for (file in files) {
            if (file.type?.let { type ->
                    supportedAudioFormats.any { type.contains(it, ignoreCase = true) }
                } == true
            ) {
                try {
                    retriever.setDataSource(context, file.uri)

                    val pictureData = retriever.embeddedPicture
                    if (pictureData != null) {
                        return BitmapFactory.decodeByteArray(
                            pictureData, 0, pictureData.size
                        ).asImageBitmap()
                    }
                } catch (e: Exception) {
                    //
                }
            }
        }
    } finally {
        retriever.release()
    }

    return null
}