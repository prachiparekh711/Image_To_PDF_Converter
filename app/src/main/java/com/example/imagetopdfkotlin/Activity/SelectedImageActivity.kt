package com.example.imagetopdfkotlin.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.*
import android.os.AsyncTask
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagetopdfkotlin.Adapter.SelectedImageAdapter
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.ActivitySelectedImageBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfWriter
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_selected_image.*
import kotlinx.android.synthetic.main.bottom_setting_layout.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class SelectedImageActivity : AppCompatActivity() {

    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    var selecteImageAdapter: SelectedImageAdapter? = null
    var orientation = "Original"
    var longOperation: LongOperation? = null
    var compressProgress = 0f

    companion object {
        var selectedImageBinding: ActivitySelectedImageBinding? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedImageBinding =
            DataBindingUtil.setContentView(
                this@SelectedImageActivity,
                R.layout.activity_selected_image
            )

        for (i in Util.mNewSelectedImageList.indices) {
            if (!Util.mEditedImageList.contains(Util.mNewSelectedImageList[i]))
                Util.mEditedImageList.add(Util.mNewSelectedImageList[i])
        }

        selectedImageBinding?.title?.setOnClickListener { onBackPressed() }

        selectedImageBinding?.mSelection?.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            startActivity(intent)
        }

        selectedImageBinding?.mEdit?.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }

        selectedImageBinding?.mSort?.setOnClickListener {
            val intent = Intent(this, SortingActivity::class.java)
            startActivity(intent)
        }
        selectedImageBinding?.blankRL?.isVisible = false
        bottomSheetBehavior = BottomSheetBehavior.from(bottomCard)

        selectedImageBinding?.mConvert?.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                selectedImageBinding?.blankRL?.isVisible = false
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                selectedImageBinding?.blankRL?.isVisible = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        tvCancel.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                selectedImageBinding?.blankRL?.isVisible = false
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })

        tvOk.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                selectedImageBinding?.blankRL?.isVisible = false
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                if (!etFileName.text.toString().isEmpty()) {
                    if (switchProtect.isChecked) {
                        if (!etPassword.text.toString().isEmpty()) {
                            longOperation =
                                LongOperation(
                                    etFileName.text.toString(),
                                    compressProgress,
                                    switchMargin.isChecked,
                                    switchProtect.isChecked,
                                    etPassword.text.toString(),
                                    orientation,
                                    this@SelectedImageActivity
                                )
                            longOperation!!.execute()
                        } else {
                            Toasty.error(
                                this@SelectedImageActivity,
                                "Please enter password.",
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        longOperation =
                            LongOperation(
                                etFileName.text.toString(),
                                compressProgress,
                                switchMargin.isChecked,
                                switchProtect.isChecked,
                                etPassword.text.toString(),
                                orientation,
                                this@SelectedImageActivity
                            )
                        longOperation!!.execute()
                    }
                } else {
                    Toasty.error(
                        this@SelectedImageActivity,
                        "Please enter file name",
                        Toasty.LENGTH_SHORT
                    ).show()
                }
            }
        })

        switchCompress.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    expand(seekBarCompress)
                } else {
                    collapse(seekBarCompress)
                }
            }
        })

        switchProtect.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    expand(rlProtectPass)
                } else {
                    collapse(rlProtectPass)
                }
            }
        })

        val list = ArrayList<String>()
        list.add("Original")
        list.add("Landscape")
        list.add("Portrait")
        val adapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(
                this@SelectedImageActivity, R.layout.spinner_item,
                list as List<Any?>
            )
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                orientation = list.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                orientation = list.get(0)
            }
        }

        etFileName.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Util.hideKeyboard(etFileName, this@SelectedImageActivity)
                return@setOnEditorActionListener true
            }
            false
        }

        etPassword.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Util.hideKeyboard(etPassword, this@SelectedImageActivity)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("list size:", Util.mEditedImageList.size.toString())
        selecteImageAdapter =
            SelectedImageAdapter(this@SelectedImageActivity)
        selectedImageBinding?.mImageRec?.layoutManager =
            GridLayoutManager(this@SelectedImageActivity, 2)
        selectedImageBinding?.mImageRec?.layoutAnimation = null
        selecteImageAdapter?.newArray(0)

        if (Util.mEditedImageList != null && Util.mEditedImageList.size > 0) {

            for (i in Util.mEditedImageList.indices) {
                selecteImageAdapter?.add(i, Util.mEditedImageList[i])
            }
            selecteImageAdapter?.add(Util.mEditedImageList.size, "add_img")
        }
        selectedImageBinding?.mImageRec?.adapter = selecteImageAdapter
    }

    fun collapse(v: View) {
        val initialHeight = v.measuredHeight
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height =
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

// Collapse speed of 1dp/ms
        a.duration = 500
        v.startAnimation(a)
    }

    fun expand(v: View) {
        val matchParentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = v.measuredHeight

// Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                v.layoutParams.height =
                    if (interpolatedTime == 1f) LinearLayout.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

// Expansion speed of 1dp/ms
        a.duration = 500
        v.startAnimation(a)
    }

    class LongOperation(
        var fileName: String,
        var compressProgress: Float,
        var isMargin: Boolean,
        var isPassword: Boolean,
        var password: String,
        var orientation: String,
        var activity: Activity
    ) :
        AsyncTask<Void?, Int?, File>() {

        override fun doInBackground(vararg params: Void?): File {
            val art = ArrayList<Bitmap>()
            for (i in 0 until Util.mEditedImageList.size) {
                val bitmap = BitmapFactory.decodeFile(Util.mEditedImageList.get(i))
                if (bitmap != null) art.add(bitmap)
                if (art.size == 6) break
            }
            var document = Document()
            var fileOutputStream: FileOutputStream? = null
            val file1: File
            if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cw = ContextWrapper(activity.applicationContext)
                file1 = File(
                    cw.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                    activity.resources.getString(R.string.app_name)
                )
            } else {
                file1 = File(Util.BASE_PATH, activity.resources.getString(R.string.app_name))
            }
            if (!file1.exists()) {
                file1.mkdirs()
            }
            val file = File(file1.absolutePath, "$fileName.pdf")
            try {
                if (orientation == "Portrait") {
                    if (isMargin) document =
                        Document(PageSize.A4, 38.0f, 38.0f, 50.0f, 38.0f) else document =
                        Document(PageSize.A4, 0f, 0f, 0f, 0f)
                } else if (orientation == "Landscape") {
                    if (isMargin) document =
                        Document(PageSize.A4_LANDSCAPE, 38.0f, 38.0f, 50.0f, 38.0f) else document =
                        Document(PageSize.A4_LANDSCAPE, 0f, 0f, 0f, 0f)
                } else {
                    if (isMargin) document =
                        Document(PageSize.A4, 38.0f, 38.0f, 50.0f, 38.0f) else document =
                        Document(PageSize.A4, 0f, 0f, 0f, 0f)
                }
                fileOutputStream = FileOutputStream(file.absolutePath)
                val writer: PdfWriter =
                    PdfWriter.getInstance(document, fileOutputStream) //  Change pdf's name.
                if (isPassword) {
                    try {
                        val charset = Charsets.UTF_8
                        writer.setEncryption(
                            password.toByteArray(charset),
                            Util.MASTER_PASSWORD.toByteArray(charset),
                            PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                            PdfWriter.ENCRYPTION_AES_128
                        )
                    } catch (e: DocumentException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            document.open()
            var i = 0
            while (true) {
                if (i < Util.mEditedImageList.size) {
                    try {
                        val image: Image = Image.getInstance(
                            Util.mEditedImageList.get(i)
                        ) // Change image's name and extension.
                        val scale: Float =
                            (document.pageSize.width - document.leftMargin()
                                    - document.rightMargin() - 0) / image.width * 100 // 0 means you have no indentation. If you have any, change it.
                        image.scalePercent(scale)
                        image.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
                        image.setAbsolutePosition(
                            (document.pageSize.width - image.scaledWidth) / 2.0f,
                            (document.pageSize.height - image.scaledHeight) / 2.0f
                        )
                        val qualityMod = compressProgress * 0.09
                        image.compressionLevel = qualityMod.toInt()
                        document.add(image)
                        document.newPage()
                        publishProgress(i)
                        i++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    document.close()
                    return file
                }
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            activity.runOnUiThread(Runnable {
                selectedImageBinding?.progress?.progress =
                    ((values[0]?.plus(1))?.times(100) ?: 0) / Util.mEditedImageList.size
                val str =
                    ((values[0]?.plus(1))?.times(100))?.div(Util.mEditedImageList.size).toString()
                selectedImageBinding?.tvProgress?.text = str + "%"

                val sb = StringBuilder()
                sb.append("Processing images (")
                sb.append(values[0]?.plus(1))
                sb.append("/")
                sb.append(Util.mEditedImageList.size)
                sb.append(")")
                selectedImageBinding?.tvCount?.text = sb.toString()
            })
        }

        override fun onPostExecute(file: File) {
            super.onPostExecute(file)
            Toasty.success(activity, "Pdf Created successfully.", Toasty.LENGTH_SHORT)
                .show()
            Util.mEditedImageList.clear()
            val intent = Intent(activity, SuccessActivity::class.java)
            intent.putExtra("pdf_file", file.path)
            activity.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    private fun showExitDialog() {
        val alertadd = AlertDialog.Builder(this@SelectedImageActivity)

        alertadd.setMessage("Are you sure you want to exit?")
        alertadd.setPositiveButton(
            "Yes"
        ) { dialog, id ->
            Util.mEditedImageList.clear()
            Util.mNewSelectedImageList.clear()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertadd.setNegativeButton(
            "No"
        ) { dialog, which -> dialog.dismiss() }
        alertadd.show()
    }
}