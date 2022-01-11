package com.example.imagetopdfkotlin.Utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.imagetopdfkotlin.Adapter.PdfDocumentAdapter
import com.example.imagetopdfkotlin.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.text.DocumentException
import com.itextpdf.text.exceptions.BadPasswordException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import es.dmoral.toasty.Toasty
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

class Util {
    companion object {
        const val MASTER_PASSWORD = "master_password"
        var BASE_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()

        var mSelectedImageList = ArrayList<String>()
        var mNewSelectedImageList = ArrayList<String>()
        var mEditedImageList = ArrayList<String>()
        var itemRemoved = false
        var editedBitmap: Bitmap? = null
        var edited = false
        var finalEdit = false
        var mDoodleColor = R.color.md_grey_900
        var mPenWidth = 20f
        var json1: String? = null
        var sharedPreferences: SharedPreferences? = null
        var editor: SharedPreferences.Editor? = null
        var mFavouriteImageList = ArrayList<String>()
        var gson: Gson? = null
        var hidden = true
        var exist = false
        var pos = 0

        fun getRealPathFromURI(context: Context, uri: Uri?): String? {
            var path = ""
            if (context.contentResolver != null) {
                val cursor = context.contentResolver.query(uri!!, null, null, null, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    path = cursor.getString(idx)
                    cursor.close()
                }
            }
            return path
        }

        fun convertTimeDateModified(time: Long): String? {
            val date = Date(time * 1000)
            @SuppressLint("SimpleDateFormat") val format: Format = SimpleDateFormat("dd MMM yyyy")
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat("dd MMM yyyy")

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            return formatter.format(calendar.time)
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        fun printPDF(filePath: String, context: Context): Boolean {
            if (IsPasswordProtected(filePath)) {
                return false
            } else {
                val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

                val adapter = PdfDocumentAdapter(context, filePath)
                val attributes = PrintAttributes.Builder().build()
                manager.print("Document", adapter, attributes)
                return true
            }
        }

        fun IsPasswordProtected(pdfFullname: String?): Boolean {
            return try {
                val pdfReader = PdfReader(pdfFullname)
                false
            } catch (e: BadPasswordException) {
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        fun delete(file: File, activity: Activity) {
            val alertadd = AlertDialog.Builder(activity).create()
            val factory = LayoutInflater.from(activity)
            val view = factory.inflate(R.layout.dialog, null)
            alertadd.setView(view)
            alertadd.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val deleteCard: CardView = view.findViewById(R.id.deleteRL)
            deleteCard.visibility = View.VISIBLE
            val cancel: CardView = view.findViewById(R.id.cancel2)
            val delete: CardView = view.findViewById(R.id.delete)
            val mediaID: Long =
                Util.getFilePathToMediaID(file.absolutePath, activity)
            val Uri_one =
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.getContentUri("external"),
                    mediaID
                )
            val uris: MutableList<Uri> = ArrayList()
            uris.add(Uri_one)
            cancel.setOnClickListener { alertadd.dismiss() }
            delete.setOnClickListener {
                val isDelete = file.delete()
                if (!isDelete) {
                    Toasty.error(activity, "Something went wrong!!!").show()
                } else {
                    var mFavouriteImageList: ArrayList<String?>? =
                        ArrayList()
                    var json1: String?
                    val sharedPreferences: SharedPreferences = activity.getSharedPreferences(
                        "Favourites_pref",
                        AppCompatActivity.MODE_PRIVATE
                    )
                    val editor = sharedPreferences.edit()
                    val gson = Gson()
                    json1 = sharedPreferences.getString("Fav_Image", "")
                    val type1 = object : TypeToken<ArrayList<String?>?>() {}.type
                    mFavouriteImageList =
                        gson.fromJson(json1, type1)
                    if (mFavouriteImageList == null) {
                        mFavouriteImageList = ArrayList()
                    }
                    if (mFavouriteImageList.contains(file.path)) {
                        mFavouriteImageList.remove(file.path)
                        json1 = gson.toJson(mFavouriteImageList)
                        editor.putString("Fav_Image", json1)
                        editor.apply()

                    }
                    Toasty.success(activity, "Delete successfully !").show()
                    val lbm = LocalBroadcastManager.getInstance(activity)
                    val localIn = Intent("REFRESH")
                    lbm.sendBroadcast(localIn)
                }
                alertadd.dismiss()
            }
            alertadd.show()
        }

        fun rename(file: File, activity: Activity) {
            val alertadd = AlertDialog.Builder(activity).create()
            val factory = LayoutInflater.from(activity)
            val view = factory.inflate(R.layout.dialog, null)
            alertadd.setView(view)
            alertadd.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val renameCard: CardView = view.findViewById(R.id.renameRL)
            renameCard.visibility = View.VISIBLE
            val cancelIcon = view.findViewById<ImageView>(R.id.cancelIcon)
            val newName = view.findViewById<EditText>(R.id.etName)
            val cancel: CardView = view.findViewById(R.id.cancel)
            val rename: CardView = view.findViewById(R.id.rename)
            var fileName = file.name
            val pos = fileName.lastIndexOf(".")
            if (pos > 0 && pos < file.name.length - 1) {
                fileName = fileName.substring(0, pos)
            }
            newName.hint = fileName
            newName.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Util.hideKeyboard(newName, activity)
                    return@setOnEditorActionListener true
                }
                false
            }
            cancel.setOnClickListener { alertadd.dismiss() }
            cancelIcon.setOnClickListener { newName.setText("") }
            rename.setOnClickListener {
                val name = newName.text.toString()
                if (name != "") {
                    val extension = file.absolutePath.substring(file.absolutePath.lastIndexOf("."))
                    val FolderPath = file.parentFile.absolutePath
                    val to = File("$FolderPath/$name$extension")
                    val isRename = file.renameTo(to)
                    if (!isRename) {
                        Toasty.error(activity, "Something went wrong!!!").show()
                    } else {
                        sharedPreferences =
                            activity.getSharedPreferences(
                                "Favourites_pref",
                                AppCompatActivity.MODE_PRIVATE
                            )
                        Util.editor = Util.sharedPreferences?.edit()
                        try {
                            Util.gson = Gson()
                            val json = Util.sharedPreferences?.getString("Fav_Image", "")
                            val type = object : TypeToken<ArrayList<String?>?>() {}.type
                            Util.mFavouriteImageList = Util.gson?.fromJson(json, type)!!

                            if (Util.mFavouriteImageList.size > 0) {
                                if (Util.mFavouriteImageList.contains(file.path)) {
                                    Util.mFavouriteImageList.remove(file.path)
                                    Util.mFavouriteImageList.add(to.path)
                                    Util.json1 = Util.gson?.toJson(Util.mFavouriteImageList)
                                    Util.editor?.putString("Fav_Image", Util.json1)
                                    Util.editor?.apply()
                                }
                            }
                        } catch (e: Exception) {
                            e.message?.let { Log.e("error", it) }
                        }

                        Toasty.success(activity, "Rename successfully !").show()
                        val lbm = LocalBroadcastManager.getInstance(activity)
                        val localIn = Intent("REFRESH")
                        lbm.sendBroadcast(localIn)
                    }
                    alertadd.dismiss()
                }
            }
            alertadd.show()
        }

        fun removePassword(file: File, activity: Activity) {
            val alertadd = AlertDialog.Builder(activity).create()
            val factory = LayoutInflater.from(activity)
            val view = factory.inflate(R.layout.dialog, null)
            alertadd.setView(view)
            alertadd.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val pswdCard: CardView = view.findViewById(R.id.removeCard)
            pswdCard.visibility = View.VISIBLE
            val hideIcon = view.findViewById<ImageView>(R.id.hideIcon1)
            val newPswd = view.findViewById<EditText>(R.id.etPassword1)
            val cancel: CardView = view.findViewById(R.id.cancel3)
            val apply: CardView = view.findViewById(R.id.remove)
            cancel.setOnClickListener { alertadd.dismiss() }
            hideIcon.setOnClickListener {
                newPswd.clearFocus()
                hidden = !hidden
                if (hidden) {
                    hideIcon.setImageDrawable(
                        activity.resources.getDrawable(R.drawable.ic_not_visible_pswd)
                    )
                    newPswd.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    hideIcon.setImageDrawable(
                        activity.resources.getDrawable(R.drawable.ic_visible_pswd)
                    )
                    newPswd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
            }
            apply.setOnClickListener {
                manipulatePdf(file.path, newPswd.text.toString(), activity)
                alertadd.dismiss()
            }
            alertadd.show()
        }

        fun manipulatePdf(src: String?, input_password: String, activity: Activity) {
            var folder: File? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cw = ContextWrapper(activity.applicationContext)
                folder = File(
                    cw.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                    activity.resources.getString(R.string.app_name)
                )
            } else {
                folder = File(Util.BASE_PATH, activity.resources.getString(R.string.app_name))
            }
            if (!folder.exists()) folder.mkdirs()
            val source = File(src)
            var fileName = source.name
            val pos = fileName.lastIndexOf(".")
            if (pos > 0 && pos < source.name.length - 1) {
                fileName = fileName.substring(0, pos)
            }
            val finalOutputFile: File
            try {
                val masterPwd = "admin"
                val reader = PdfReader(src, masterPwd.toByteArray())
                val password: ByteArray
                finalOutputFile = File(folder.absolutePath + "/" + fileName + "_decrypt" + ".pdf")
                password = reader.computeUserPassword()
                val input = input_password.toByteArray()
                if (Arrays.equals(input, password)) {
                    val stamper = PdfStamper(reader, FileOutputStream(finalOutputFile))
                    stamper.close()
                    reader.close()
                    source.delete()
                    finalOutputFile.renameTo(File(folder.absolutePath + "/" + fileName + ".pdf"))
                    Toasty.success(activity, "Password remove successfully !").show()
                } else {
                    Toasty.error(activity, "Password is incorrect!!!").show()
                }
            } catch (e: DocumentException) {
                e.printStackTrace()
                Toasty.error(activity, "Something went wrong!!!").show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toasty.error(activity, "Something went wrong!!!").show()
            }
        }

        fun addPassword(file: File, activity: Activity) {
            val alertadd = AlertDialog.Builder(activity).create()
            val factory = LayoutInflater.from(activity)
            val view = factory.inflate(R.layout.dialog, null)
            alertadd.setView(view)
            alertadd.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val pswdCard: CardView = view.findViewById(R.id.pswdCard)
            pswdCard.visibility = View.VISIBLE
            val hideIcon = view.findViewById<ImageView>(R.id.hideIcon)
            val pswdText = view.findViewById<TextView>(R.id.pswdText)
            val newPswd = view.findViewById<EditText>(R.id.etPassword)
            val cancel: CardView = view.findViewById(R.id.cancel1)
            val generate: CardView = view.findViewById(R.id.generate)
            pswdText.text = (activity.resources.getString(R.string.add_pswd_dialog)
                    + " " + file.name)
            cancel.setOnClickListener { alertadd.dismiss() }
            hideIcon.setOnClickListener {
                newPswd.clearFocus()
                hidden = !hidden
                if (hidden) {
                    hideIcon.setImageDrawable(
                        activity.resources.getDrawable(R.drawable.ic_not_visible_pswd)
                    )
                    newPswd.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    hideIcon.setImageDrawable(
                        activity.resources.getDrawable(R.drawable.ic_visible_pswd)
                    )
                    newPswd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }
            }
            generate.setOnClickListener {
                encryptPdf(file.path, newPswd.text.toString(), activity)
                alertadd.dismiss()
            }
            alertadd.show()
        }

        fun encryptPdf(src: String?, pswd: String, activity: Activity) {
            val source = File(src)
            var reader: PdfReader? = null
            var folder: File? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cw = ContextWrapper(activity.applicationContext)
                folder = File(
                    cw.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                    activity.resources.getString(R.string.app_name)
                )
            } else {
                folder = File(Util.BASE_PATH, activity.resources.getString(R.string.app_name))
            }
            if (!folder.exists()) folder.mkdirs()
            var fileName = source.name
            val pos = fileName.lastIndexOf(".")
            if (pos > 0 && pos < source.name.length - 1) {
                fileName = fileName.substring(0, pos)
            }
            val dest =
                File(folder.absolutePath + "/" + fileName + "_" + System.currentTimeMillis() + ".pdf")
            try {
                reader = PdfReader(src)
                val stamper = PdfStamper(reader, FileOutputStream(dest))
                stamper.setEncryption(
                    pswd.toByteArray(), "admin".toByteArray(),
                    PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                    PdfWriter.ENCRYPTION_AES_128
                )
                stamper.close()
                reader.close()
                source.delete()
                dest.renameTo(File(folder.absolutePath + "/" + fileName + ".pdf"))
                Toasty.success(activity, "Password set successfully !").show()
                val lbm = LocalBroadcastManager.getInstance(activity)
                val localIn = Intent("REFRESH")
                lbm.sendBroadcast(localIn)
            } catch (e: DocumentException) {
                e.printStackTrace()
                Log.e("DocumentException ", e.message!!)
                Toasty.error(activity, "Something went wrong!!!").show()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Log.e("FileNotFoundException ", e.message!!)
                Toasty.error(activity, "Something went wrong!!!").show()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("IOException ", e.message!!)
                Toasty.error(activity, "Something went wrong!!!").show()
            }
        }

        fun bookmark(file: File, context: Context) {
            sharedPreferences = context.getSharedPreferences(
                "Favourites_pref",
                AppCompatActivity.MODE_PRIVATE
            )
            editor = sharedPreferences?.edit()
            try {
                gson = Gson()
                val json = sharedPreferences?.getString("Fav_Image", "")
                val type = object : TypeToken<ArrayList<String?>?>() {}.type
                mFavouriteImageList = gson?.fromJson(json, type)!!
            } catch (e: Exception) {
                e.message?.let { Log.e("error", it) }
            }
            if (mFavouriteImageList.size > 0) {
                for (i in mFavouriteImageList.indices) {
                    if (mFavouriteImageList.get(i) == file.path) {
                        exist = true
                        pos = i
                        break
                    } else {
                        exist = false
                    }
                }
            }
            if (!exist) {
                file.path.let { mFavouriteImageList.add(it) }
                Toasty.success(context, "Bookmark successfully !").show()
            } else {
                mFavouriteImageList.removeAt(pos)
                exist = false
                Toasty.success(context, "Bookmark removed!").show()
            }

//        Log.e("fav_size", mFavouriteImageList.size.toString())
            json1 = gson?.toJson(mFavouriteImageList)
            editor?.putString("Fav_Image", json1)
            editor?.apply()


            val lbm = LocalBroadcastManager.getInstance(context)
            val localIn = Intent("REFRESH")
            lbm.sendBroadcast(localIn)
        }


        fun hideKeyboard(view: View?, activity: Activity) {
            if (view != null) {
                val imm =
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun getFilePathToMediaID(songPath: String, context: Context): Long {
            var id: Long = 0
            val cr = context.contentResolver
            val uri = MediaStore.Files.getContentUri("external")
            val selection = MediaStore.Images.Media.DATA
            val selectionArgs = arrayOf(songPath)
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val sortOrder = MediaStore.Images.Media.TITLE + " ASC"
            val cursor = cr.query(
                uri, projection,
                "$selection=?", selectionArgs, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    id = cursor.getString(idIndex).toLong()
                }
            }
            return id
        }

        fun initColorList(): ArrayList<Int>? {
            val colorList = ArrayList<Int>()

            colorList.add(R.color.md_grey_900)
            colorList.add(R.color.md_grey_800)
            colorList.add(R.color.md_grey_700)
            colorList.add(R.color.md_grey_600)
            colorList.add(R.color.md_grey_500)
            colorList.add(R.color.md_grey_400)
            colorList.add(R.color.md_grey_300)
            colorList.add(R.color.md_grey_200)
            colorList.add(R.color.md_grey_100)
            colorList.add(R.color.md_grey_50)

            colorList.add(R.color.md_amber_50)
            colorList.add(R.color.md_amber_100)
            colorList.add(R.color.md_amber_200)
            colorList.add(R.color.md_amber_300)
            colorList.add(R.color.md_amber_400)
            colorList.add(R.color.md_amber_500)
            colorList.add(R.color.md_amber_600)
            colorList.add(R.color.md_amber_700)
            colorList.add(R.color.md_amber_800)
            colorList.add(R.color.md_amber_900)
            colorList.add(R.color.md_amber_A100)
            colorList.add(R.color.md_amber_A200)
            colorList.add(R.color.md_amber_A400)
            colorList.add(R.color.md_amber_A700)
            colorList.add(R.color.md_blue_50)
            colorList.add(R.color.md_blue_100)
            colorList.add(R.color.md_blue_200)
            colorList.add(R.color.md_blue_300)
            colorList.add(R.color.md_blue_400)
            colorList.add(R.color.md_blue_500)
            colorList.add(R.color.md_blue_600)
            colorList.add(R.color.md_blue_700)
            colorList.add(R.color.md_blue_800)
            colorList.add(R.color.md_blue_900)
            colorList.add(R.color.md_blue_A100)
            colorList.add(R.color.md_blue_A200)
            colorList.add(R.color.md_blue_A400)
            colorList.add(R.color.md_blue_A700)
            colorList.add(R.color.md_blue_grey_50)
            colorList.add(R.color.md_blue_grey_100)
            colorList.add(R.color.md_blue_grey_200)
            colorList.add(R.color.md_blue_grey_300)
            colorList.add(R.color.md_blue_grey_400)
            colorList.add(R.color.md_blue_grey_500)
            colorList.add(R.color.md_blue_grey_600)
            colorList.add(R.color.md_blue_grey_700)
            colorList.add(R.color.md_blue_grey_800)
            colorList.add(R.color.md_blue_grey_900)
            colorList.add(R.color.md_brown_50)
            colorList.add(R.color.md_brown_100)
            colorList.add(R.color.md_brown_200)
            colorList.add(R.color.md_brown_300)
            colorList.add(R.color.md_brown_400)
            colorList.add(R.color.md_brown_500)
            colorList.add(R.color.md_brown_600)
            colorList.add(R.color.md_brown_700)
            colorList.add(R.color.md_brown_800)
            colorList.add(R.color.md_brown_900)
            colorList.add(R.color.md_cyan_50)
            colorList.add(R.color.md_cyan_100)
            colorList.add(R.color.md_cyan_200)
            colorList.add(R.color.md_cyan_300)
            colorList.add(R.color.md_cyan_400)
            colorList.add(R.color.md_cyan_500)
            colorList.add(R.color.md_cyan_600)
            colorList.add(R.color.md_cyan_700)
            colorList.add(R.color.md_cyan_800)
            colorList.add(R.color.md_cyan_900)
            colorList.add(R.color.md_cyan_A100)
            colorList.add(R.color.md_cyan_A200)
            colorList.add(R.color.md_cyan_A400)
            colorList.add(R.color.md_cyan_A700)
            colorList.add(R.color.md_deep_orange_50)
            colorList.add(R.color.md_deep_orange_100)
            colorList.add(R.color.md_deep_orange_200)
            colorList.add(R.color.md_deep_orange_300)
            colorList.add(R.color.md_deep_orange_400)
            colorList.add(R.color.md_deep_orange_500)
            colorList.add(R.color.md_deep_orange_600)
            colorList.add(R.color.md_deep_orange_700)
            colorList.add(R.color.md_deep_orange_800)
            colorList.add(R.color.md_deep_orange_900)
            colorList.add(R.color.md_deep_orange_A100)
            colorList.add(R.color.md_deep_orange_A200)
            colorList.add(R.color.md_deep_orange_A400)
            colorList.add(R.color.md_deep_orange_A700)
            colorList.add(R.color.md_deep_purple_50)
            colorList.add(R.color.md_deep_purple_100)
            colorList.add(R.color.md_deep_purple_200)
            colorList.add(R.color.md_deep_purple_300)
            colorList.add(R.color.md_deep_purple_400)
            colorList.add(R.color.md_deep_purple_500)
            colorList.add(R.color.md_deep_purple_600)
            colorList.add(R.color.md_deep_purple_700)
            colorList.add(R.color.md_deep_purple_800)
            colorList.add(R.color.md_deep_purple_900)
            colorList.add(R.color.md_deep_purple_A100)
            colorList.add(R.color.md_deep_purple_A200)
            colorList.add(R.color.md_deep_purple_A400)
            colorList.add(R.color.md_deep_purple_A700)
            colorList.add(R.color.md_green_50)
            colorList.add(R.color.md_green_100)
            colorList.add(R.color.md_green_200)
            colorList.add(R.color.md_green_300)
            colorList.add(R.color.md_green_400)
            colorList.add(R.color.md_green_500)
            colorList.add(R.color.md_green_600)
            colorList.add(R.color.md_green_700)
            colorList.add(R.color.md_green_800)
            colorList.add(R.color.md_green_900)
            colorList.add(R.color.md_green_A100)
            colorList.add(R.color.md_green_A200)
            colorList.add(R.color.md_green_A400)
            colorList.add(R.color.md_green_A700)

            colorList.add(R.color.md_indigo_50)
            colorList.add(R.color.md_indigo_100)
            colorList.add(R.color.md_indigo_200)
            colorList.add(R.color.md_indigo_300)
            colorList.add(R.color.md_indigo_400)
            colorList.add(R.color.md_indigo_500)
            colorList.add(R.color.md_indigo_600)
            colorList.add(R.color.md_indigo_700)
            colorList.add(R.color.md_indigo_800)
            colorList.add(R.color.md_indigo_900)
            colorList.add(R.color.md_indigo_A100)
            colorList.add(R.color.md_indigo_A200)
            colorList.add(R.color.md_indigo_A400)
            colorList.add(R.color.md_indigo_A700)
            colorList.add(R.color.md_light_blue_50)
            colorList.add(R.color.md_light_blue_100)
            colorList.add(R.color.md_light_blue_200)
            colorList.add(R.color.md_light_blue_300)
            colorList.add(R.color.md_light_blue_400)
            colorList.add(R.color.md_light_blue_500)
            colorList.add(R.color.md_light_blue_600)
            colorList.add(R.color.md_light_blue_700)
            colorList.add(R.color.md_light_blue_800)
            colorList.add(R.color.md_light_blue_900)
            colorList.add(R.color.md_light_blue_A100)
            colorList.add(R.color.md_light_blue_A200)
            colorList.add(R.color.md_light_blue_A400)
            colorList.add(R.color.md_light_blue_A700)
            colorList.add(R.color.md_light_green_50)
            colorList.add(R.color.md_light_green_100)
            colorList.add(R.color.md_light_green_200)
            colorList.add(R.color.md_light_green_300)
            colorList.add(R.color.md_light_green_400)
            colorList.add(R.color.md_light_green_500)
            colorList.add(R.color.md_light_green_600)
            colorList.add(R.color.md_light_green_700)
            colorList.add(R.color.md_light_green_800)
            colorList.add(R.color.md_light_green_900)
            colorList.add(R.color.md_light_green_A100)
            colorList.add(R.color.md_light_green_A200)
            colorList.add(R.color.md_light_green_A400)
            colorList.add(R.color.md_light_green_A700)
            colorList.add(R.color.md_lime_50)
            colorList.add(R.color.md_lime_100)
            colorList.add(R.color.md_lime_200)
            colorList.add(R.color.md_lime_300)
            colorList.add(R.color.md_lime_400)
            colorList.add(R.color.md_lime_500)
            colorList.add(R.color.md_lime_600)
            colorList.add(R.color.md_lime_700)
            colorList.add(R.color.md_lime_800)
            colorList.add(R.color.md_lime_900)
            colorList.add(R.color.md_lime_A100)
            colorList.add(R.color.md_lime_A200)
            colorList.add(R.color.md_lime_A400)
            colorList.add(R.color.md_lime_A700)
            colorList.add(R.color.md_orange_50)
            colorList.add(R.color.md_orange_100)
            colorList.add(R.color.md_orange_200)
            colorList.add(R.color.md_orange_300)
            colorList.add(R.color.md_orange_400)
            colorList.add(R.color.md_orange_500)
            colorList.add(R.color.md_orange_600)
            colorList.add(R.color.md_orange_700)
            colorList.add(R.color.md_orange_800)
            colorList.add(R.color.md_orange_900)
            colorList.add(R.color.md_orange_A100)
            colorList.add(R.color.md_orange_A200)
            colorList.add(R.color.md_orange_A400)
            colorList.add(R.color.md_orange_A700)
            colorList.add(R.color.md_pink_50)
            colorList.add(R.color.md_pink_100)
            colorList.add(R.color.md_pink_200)
            colorList.add(R.color.md_pink_300)
            colorList.add(R.color.md_pink_400)
            colorList.add(R.color.md_pink_500)
            colorList.add(R.color.md_pink_600)
            colorList.add(R.color.md_pink_700)
            colorList.add(R.color.md_pink_800)
            colorList.add(R.color.md_pink_900)
            colorList.add(R.color.md_pink_A100)
            colorList.add(R.color.md_pink_A200)
            colorList.add(R.color.md_pink_A400)
            colorList.add(R.color.md_pink_A700)
            colorList.add(R.color.md_purple_50)
            colorList.add(R.color.md_purple_100)
            colorList.add(R.color.md_purple_200)
            colorList.add(R.color.md_purple_300)
            colorList.add(R.color.md_purple_400)
            colorList.add(R.color.md_purple_500)
            colorList.add(R.color.md_purple_600)
            colorList.add(R.color.md_purple_700)
            colorList.add(R.color.md_purple_800)
            colorList.add(R.color.md_purple_900)
            colorList.add(R.color.md_purple_A100)
            colorList.add(R.color.md_purple_A200)
            colorList.add(R.color.md_purple_A400)
            colorList.add(R.color.md_purple_A700)
            colorList.add(R.color.md_red_50)
            colorList.add(R.color.md_red_100)
            colorList.add(R.color.md_red_200)
            colorList.add(R.color.md_red_300)
            colorList.add(R.color.md_red_400)
            colorList.add(R.color.md_red_500)
            colorList.add(R.color.md_red_600)
            colorList.add(R.color.md_red_700)
            colorList.add(R.color.md_red_800)
            colorList.add(R.color.md_red_900)
            colorList.add(R.color.md_red_A100)
            colorList.add(R.color.md_red_A200)
            colorList.add(R.color.md_red_A400)
            colorList.add(R.color.md_red_A700)
            colorList.add(R.color.md_teal_50)
            colorList.add(R.color.md_teal_100)
            colorList.add(R.color.md_teal_200)
            colorList.add(R.color.md_teal_300)
            colorList.add(R.color.md_teal_400)
            colorList.add(R.color.md_teal_500)
            colorList.add(R.color.md_teal_600)
            colorList.add(R.color.md_teal_700)
            colorList.add(R.color.md_teal_800)
            colorList.add(R.color.md_teal_900)
            colorList.add(R.color.md_teal_A100)
            colorList.add(R.color.md_teal_A200)
            colorList.add(R.color.md_teal_A400)
            colorList.add(R.color.md_teal_A700)
            return colorList
        }
    }

}