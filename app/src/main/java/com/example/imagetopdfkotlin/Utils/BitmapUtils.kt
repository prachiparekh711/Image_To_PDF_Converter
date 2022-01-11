package com.example.imagetopdfkotlin.Utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

object BitmapUtils {
    private val TAG = BitmapUtils::class.java.simpleName

    /**
     * Getting bitmap from Assets folder
     *
     * @return
     */
    fun getBitmapFromAssets(context: Context, fileName: String?, width: Int, height: Int): Bitmap? {
        val assetManager = context.assets
        val istr: InputStream
        val bitmap: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            istr = assetManager.open(fileName!!)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, width, height)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeStream(istr, null, options)
        } catch (e: IOException) {
//            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return null
    }

    /**
     * Getting bitmap from Gallery
     *
     * @return
     */
    fun getBitmapFromGallery(context: Context, path: Uri?, width: Int, height: Int): Bitmap {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(path!!, filePathColumn, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val picturePath = cursor.getString(columnIndex)
        cursor.close()
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(picturePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(picturePath, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight
                && halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(
        res: Resources?, resId: Int,
        reqWidth: Int, reqHeight: Int
    ): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    /**
     * Storing image to device gallery
     *
     * @param cr
     * @param source
     * @param title
     * @param description
     * @return
     */
    fun insertImage(
        cr: ContentResolver,
        source: Bitmap?,
        title: String?,
        description: String?
    ): String? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, title)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
        values.put(MediaStore.Images.Media.DESCRIPTION, description)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        var url: Uri? = null
        var stringUrl: String? = null /* value to be returned */
        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (source != null) {
                val imageOut = cr.openOutputStream(url!!)
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut)
                } finally {
                    imageOut!!.close()
                }
                val id = ContentUris.parseId(url)
                // Wait until MINI_KIND thumbnail is generated.
                val miniThumb = MediaStore.Images.Thumbnails.getThumbnail(
                    cr,
                    id,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null
                )
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50f, 50f, MediaStore.Images.Thumbnails.MICRO_KIND)
            } else {
                cr.delete(url!!, null, null)
                url = null
            }
        } catch (e: Exception) {
            if (url != null) {
                cr.delete(url, null, null)
                url = null
            }
        }
        if (url != null) {
            stringUrl = url.toString()
        }
        return stringUrl
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     *
     * @see MediaStore.Images.Media
     */
    private fun storeThumbnail(
        cr: ContentResolver,
        source: Bitmap,
        id: Long,
        width: Float,
        height: Float,
        kind: Int
    ): Bitmap? {

        // create the matrix to scale it
        val matrix = Matrix()
        val scaleX = width / source.width
        val scaleY = height / source.height
        matrix.setScale(scaleX, scaleY)
        val thumb = Bitmap.createBitmap(
            source, 0, 0,
            source.width,
            source.height, matrix,
            true
        )
        val values = ContentValues(4)
        values.put(MediaStore.Images.Thumbnails.KIND, kind)
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, id.toInt())
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.height)
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.width)
        val url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values)
        return try {
            val thumbOut = cr.openOutputStream(url!!)
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut)
            thumbOut!!.close()
            thumb
        } catch (ex: FileNotFoundException) {
            null
        } catch (ex: IOException) {
            null
        }
    }
}
