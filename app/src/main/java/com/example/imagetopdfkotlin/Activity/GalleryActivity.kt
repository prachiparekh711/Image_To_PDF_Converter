package com.example.imagetopdfkotlin.Activity

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagetopdfkotlin.Adapter.FolderAdapter
import com.example.imagetopdfkotlin.Interface.FolderInterface
import com.example.imagetopdfkotlin.Model.BaseModel
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.databinding.ActivityGalleryBinding
import java.io.File
import java.util.*

class GalleryActivity : AppCompatActivity(), View.OnClickListener {

    var galleryBinding: ActivityGalleryBinding? = null
    var folderInterface: FolderInterface? = null

    var folderAdapter: FolderAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryBinding =
            DataBindingUtil.setContentView(this@GalleryActivity, R.layout.activity_gallery)

        galleryBinding?.title?.setOnClickListener(this@GalleryActivity)
        setFolders()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.title -> {
                onBackPressed()
            }
        }
    }

    fun setFolders() {
        folderInterface = object : FolderInterface {
            override fun OnSelectFolder(model: BaseModel?) {
                val intent = Intent(this@GalleryActivity, InnerFolderActivity::class.java)
                intent.putExtra("baseModel", model)
                startActivity(intent)
            }
        }
        folderAdapter = FolderAdapter(this@GalleryActivity, folderInterface)
        folderAdapter?.arrayList?.clear()
        galleryBinding?.folderRec?.layoutManager = GridLayoutManager(baseContext, 2)
        galleryBinding?.folderRec?.layoutAnimation = null
        galleryBinding?.folderRec?.adapter = folderAdapter
        GetFolderList(this@GalleryActivity, folderAdapter!!).execute()
    }

    class GetFolderList(var context: Context, var folderAdapter: FolderAdapter) :
        AsyncTask<String?, Int?, ArrayList<BaseModel>?>() {
        var folderListArray: java.util.ArrayList<BaseModel>? = java.util.ArrayList()
        var TotalPhotos = 0

        override fun onPostExecute(list: java.util.ArrayList<BaseModel>?) {
            super.onPostExecute(list)
            if (folderListArray != null && folderListArray!!.size > 0) {
                for (i in folderListArray!!.indices) {
                    folderAdapter.add(i, folderListArray!![i])
                }
            }
//            stopAnim()
        }

        override fun doInBackground(vararg params: String?): java.util.ArrayList<BaseModel>? {
            try {
                TotalPhotos = 0
                val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATA
                )
                val selection =
                    MediaColumns.MIME_TYPE + "=? or " + MediaColumns.MIME_TYPE + "=? or " + MediaColumns.MIME_TYPE + "=? or " + MediaColumns.MIME_TYPE + "=?"
                val selectionArgs: Array<String>
                selectionArgs = arrayOf("image/jpeg", "image/png", "image/jpg", "image/gif")
                val cursor: Cursor? = context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    MediaColumns.DATE_ADDED + " DESC"
                )
                val ids = java.util.ArrayList<String>()
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val album = BaseModel()
                        var columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                        val columnIndexName =
                            cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                        album.bucketId = cursor.getString(columnIndex)
                        val fname = cursor.getString(columnIndexName)
                        if (cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)) != null) {
                            if (!fname.startsWith(".")) {
                                if (!ids.contains(album.bucketId)) {
                                    columnIndex =
                                        cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                                    album.bucketName = cursor.getString(columnIndex)
                                    if (cursor.getString(columnIndex) != null) {
                                        val column_index =
                                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                                        val result = cursor.getString(column_index)
                                        val ParentPath: String = GetParentPath(result)
                                        album.bucketPath = ParentPath
                                        columnIndex =
                                            cursor.getColumnIndex(MediaStore.Images.Media._ID)
                                        album.id = cursor.getString(columnIndex)
                                        album.pathlist = getCameraCover("" + album.bucketId)
                                        album.type = 0
                                        album.folderName = result
                                        if (album.pathlist?.size!! > 0) {
                                            folderListArray!!.add(album)
                                            ids.add(album.bucketId ?: "null")
                                        }
                                    }
                                    TotalPhotos += album.pathlist?.size ?: 0
                                }
                            }
                        }
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                Log.e("Error ", e.message!!)
            }
            return folderListArray
        }

        fun GetParentPath(path: String?): String {
            val file = File(path)
            return file.absoluteFile.parent
        }

        fun getCameraCover(id: String): java.util.ArrayList<String>? {
            var data: String? = null
            val result = java.util.ArrayList<String>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )
            val selection = MediaStore.Images.Media.BUCKET_ID + " = ?"
            val selectionArgs = arrayOf(id)
            val orderBy = MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                orderBy
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val name =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    do {
                        data = cursor.getString(dataColumn)
                        val filePath = File(data)
                        val length = filePath.length().toDouble()
                        if (length > 0) {
                            if (!filePath.name.startsWith(".")) {
                                if (filePath.path.lowercase(Locale.getDefault()).contains(".jpg")
                                    || filePath.path.lowercase(Locale.getDefault()).contains(".png")
                                    || filePath.path.lowercase(Locale.getDefault())
                                        .contains(".jpeg")
                                    || filePath.path.lowercase(Locale.getDefault()).contains(".gif")
                                ) {
                                    result.add(data)
                                }
                            }
                        }
                        //---------------------------------------------
                    } while (cursor.moveToNext())
                }
            }
            if (cursor != null) {
                cursor.close()
            }
            return result
        }
    }


}