package com.example.imagetopdfkotlin.Activity

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.databinding.ActivitySuccessBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_success.*
import java.io.File
import java.util.*


class SuccessActivity : AppCompatActivity(), View.OnClickListener {
    var successBinding: ActivitySuccessBinding? = null
    var pdfStr: String? = null
    var mFavouriteImageList = ArrayList<String>()
    var json1: String? = null
    var pdfFile: File? = null
    var exist = false
    var pos = 0
    var gson: Gson? = null
    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        successBinding =
            DataBindingUtil.setContentView(
                this@SuccessActivity,
                R.layout.activity_success
            )

        pdfStr = intent.getStringExtra("pdf_file")
        pdfFile = File(pdfStr)
        successBinding?.etFileName?.hint = pdfFile!!.name
        successBinding?.etFilePath?.hint = pdfFile!!.path

        successBinding?.mRename?.setOnClickListener(this)
        successBinding?.mBookmark?.setOnClickListener(this)
        successBinding?.mOpen?.setOnClickListener(this)
        successBinding?.mShare?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mRename -> {
                pdfFile?.let { rename(it) }
            }
            R.id.mBookmark -> {
                sharedPreferences = getSharedPreferences("Favourites_pref", MODE_PRIVATE)
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
                        if (mFavouriteImageList.get(i) == pdfFile?.path) {
                            exist = true
                            pos = i
                            break
                        } else {
                            exist = false
                        }
                    }
                }
                if (!exist) {
                    pdfFile?.path?.let { mFavouriteImageList.add(it) }
                    successBinding?.mBookmarkImg?.setColorFilter(
                        resources.getColor(R.color.theme_color1),
                        PorterDuff.Mode.SRC_IN
                    )
                    successBinding?.mBookmarkText?.setTextColor(resources.getColor(R.color.theme_color1))
                    successBinding?.mBookmarkText?.text = "Bookmarked"
                } else {
                    mFavouriteImageList.removeAt(pos)
                    successBinding?.mBookmarkImg?.setColorFilter(
                        resources.getColor(R.color.text_color),
                        PorterDuff.Mode.SRC_IN
                    )
                    successBinding?.mBookmarkText?.setTextColor(resources.getColor(R.color.text_color))
                    successBinding?.mBookmarkText?.text = "Bookmark"
                    exist = false
                }

                json1 = gson?.toJson(mFavouriteImageList)
                editor?.putString("Fav_Image", json1)
                editor?.apply()

            }
            R.id.mOpen -> {
                val viewPdf = Intent(Intent.ACTION_VIEW)
                viewPdf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                val URI = pdfFile?.let {
                    FileProvider.getUriForFile(
                        this@SuccessActivity,
                        applicationContext.packageName + ".provider",
                        it
                    )
                }
                viewPdf.setDataAndType(URI, "application/pdf")
                viewPdf.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(viewPdf)
            }
            R.id.mShare -> {
                val intentShareFile = Intent(Intent.ACTION_SEND)
                if (pdfFile?.exists() == true) {
                    val URI = FileProvider.getUriForFile(
                        this@SuccessActivity,
                        packageName + ".provider",
                        pdfFile!!
                    )
                    intentShareFile.type = "application/pdf"
                    intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, URI)
                    intentShareFile.putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Sharing File..."
                    )
                    intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
                    startActivity(Intent.createChooser(intentShareFile, "Share File"))
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@SuccessActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun rename(file1: File) {
        val alertadd = AlertDialog.Builder(this).create()
        val factory = LayoutInflater.from(this@SuccessActivity)
        val view: View = factory.inflate(R.layout.dialog, null)
        alertadd.setView(view)
        alertadd.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val renameCard: CardView = view.findViewById(R.id.renameRL)
        renameCard.visibility = View.VISIBLE
        val cancelIcon = view.findViewById<ImageView>(R.id.cancelIcon)
        val newName = view.findViewById<EditText>(R.id.etName)
        val cancel: CardView = view.findViewById(R.id.cancel)
        val rename: CardView = view.findViewById(R.id.rename)
        var fileName = file1.name
        val pos = fileName.lastIndexOf(".")
        if (pos > 0 && pos < file1.name.length - 1) {
            fileName = fileName.substring(0, pos)
        }
        newName.hint = fileName
        cancel.setOnClickListener { alertadd.dismiss() }
        cancelIcon.setOnClickListener { newName.setText("") }
        rename.setOnClickListener {
            val name = newName.text.toString()
            if (name != "") {
                val extension = file1.absolutePath.substring(file1.absolutePath.lastIndexOf("."))
                val FolderPath = file1.parentFile.absolutePath
                val to = File("$FolderPath/$name$extension")
                val isRename = file1.renameTo(to)
                if (!isRename) {
                    Toasty.error(this@SuccessActivity, "Something went wrong!!!").show()
                } else {
                    pdfFile = to
                    successBinding?.etFileName?.setText(pdfFile?.name)
                    Toasty.success(this@SuccessActivity, "Rename successfully !").show()
                }
                alertadd.dismiss()
            }
        }
        alertadd.show()
    }
}