package com.example.imagetopdfkotlin.Adapter

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


@RequiresApi(Build.VERSION_CODES.KITKAT)
class PdfDocumentAdapter(ctxt: Context?, pathName: String) :
    PrintDocumentAdapter() {
    var context: Context? = null
    var pathName = ""
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(" file name")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        callback.onLayoutFinished(info, oldAttributes != newAttributes)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = FileInputStream(pathName)
            outputStream = FileOutputStream(destination.fileDescriptor)

            inputStream.copyTo(outputStream)

            if (cancellationSignal.isCanceled) {
                callback.onWriteCancelled()
            } else {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        } catch (ex: RuntimeException) {
            callback.onWriteFailed(ex.message)
            Log.e("PDFDocumentAdapter", "Could not write: ${ex.localizedMessage}")
        } catch (ex: Exception) {
            callback.onWriteFailed(ex.message)
            Log.e("PDFDocumentAdapter", "Could not write: ${ex.localizedMessage}")
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    init {
        context = ctxt
        this.pathName = pathName
    }
}