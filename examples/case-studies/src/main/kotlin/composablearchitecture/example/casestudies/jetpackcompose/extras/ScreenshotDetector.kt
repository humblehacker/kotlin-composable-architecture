package composablearchitecture.example.casestudies.jetpackcompose.extras

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// Code adapted from:
// https://github.com/nikit19/ScreenshotDetector/blob/bdd587dcd5a17737cbf7531ce168f1f40740f9b7/app/src/main/java/com/example/screenshotdetector/ScreenshotDetector.kt

class ScreenshotDetector(private val context: Context) {

    private var contentObserver: ContentObserver? = null

    private val mutableScreenshotTaken: Flow<String> = callbackFlow {
        start(onChange = { trySend(it) })
        awaitClose { stop() }
    }

    val screenshotTaken: Flow<String?>
        get() = mutableScreenshotTaken.distinctUntilChanged()

    private fun start(onChange: (String) -> Unit) {
        if (contentObserver == null) {
            contentObserver = context.contentResolver.registerObserver(onChange)
        }
    }

    private fun stop() {
        contentObserver?.let { context.contentResolver.unregisterContentObserver(it) }
        contentObserver = null
    }

    private fun queryScreenshots(uri: Uri): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryRelativeDataColumn(uri)
        } else {
            queryDataColumn(uri)
        }
    }

    private fun queryDataColumn(uri: Uri): String? {
        val projection = arrayOf(
            MediaStore.Images.Media.DATA
        )
        return context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                if (path.contains("screenshot", true)) {
                    return path
                }
            }
            return null
        }
    }

    private fun queryRelativeDataColumn(uri: Uri): String? {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        return context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val relativePathColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            val displayNameColumn =
                cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val name = cursor.getString(displayNameColumn)
                val relativePath = cursor.getString(relativePathColumn)
                if (name.contains("screenshot", true)) {
                    return name
                } else if (relativePath.contains("screenshot", true)) {
                    return relativePath
                }
            }
            return null
        }
    }

    private fun ContentResolver.registerObserver(onChange: (String) -> Unit): ContentObserver {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri
                    ?.let { queryScreenshots(it) }
                    ?.let { onChange(it) }
            }
        }
        registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
        return contentObserver
    }
}
