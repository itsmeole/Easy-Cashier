package com.example.util

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

data class FileExportResult(
    val success: Boolean,
    val file: File? = null,
    val publicUri: Uri? = null,
    val fileName: String = "",
    val savedPathDisplay: String = "",
    val errorMessage: String? = null
)

object ReportFileManager {

    private const val EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    /**
     * Saves the Excel file into the app's cache directory (for FileProvider)
     * and exports a copy to the device's public Downloads directory.
     */
    fun saveExcelReport(
        context: Context,
        fileName: String,
        writeAction: (OutputStream) -> Unit
    ): FileExportResult {
        return try {
            val reportDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val cacheFile = File(reportDir, fileName)

            FileOutputStream(cacheFile).use { out ->
                writeAction(out)
            }

            var savedLocation = "Folder Download"
            var mediaUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, EXCEL_MIME)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                mediaUri = resolver.insert(collection, contentValues)

                mediaUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { out ->
                        cacheFile.inputStream().use { inp ->
                            inp.copyTo(out)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                    val publicFile = File(downloadsDir, fileName)
                    FileOutputStream(publicFile).use { out ->
                        cacheFile.inputStream().use { inp ->
                            inp.copyTo(out)
                        }
                    }
                    savedLocation = publicFile.absolutePath
                }
            }

            FileExportResult(
                success = true,
                file = cacheFile,
                publicUri = mediaUri,
                fileName = fileName,
                savedPathDisplay = savedLocation
            )
        } catch (e: Exception) {
            e.printStackTrace()
            FileExportResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Gagal menyimpan file Excel"
            )
        }
    }

    /**
     * Opens the Excel file in an external viewer app (Excel, Google Sheets, WPS Office, etc.)
     */
    fun openExcelFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, EXCEL_MIME)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Aplikasi pembuka Excel (Google Sheets / Microsoft Excel) tidak ditemukan",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal membuka file: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Shares the Excel file via Android Sharesheet (WhatsApp, Email, Drive, etc.)
     */
    fun shareExcelFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = EXCEL_MIME
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Transaksi Easy Cashier")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Bagikan Laporan Excel").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Gagal membagikan file: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
