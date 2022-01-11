package com.example.imagetopdfkotlin.Activity

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.imagetopdfkotlin.Adapter.ColorAdapter
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.Utils.Util.Companion.mDoodleColor
import com.example.imagetopdfkotlin.Utils.Util.Companion.mPenWidth
import com.example.imagetopdfkotlin.databinding.ActivityDrawBinding
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditor.OnSaveListener
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import java.io.File
import java.util.*


class DrawActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    var drawBinding: ActivityDrawBinding? = null
    var pos: Int = 0
    var colorAdapter: ColorAdapter? = null
    var colorList: ArrayList<Int>? = null
    var mPhotoEditor: PhotoEditor? = null
    var mPhotoEditor1: PhotoEditor? = null
    var mShapeBuilder: ShapeBuilder? = null
    var path: File? = null
    var fileName: String? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_draw)

        pos = intent.getIntExtra("currentPosition", 0)

        drawBinding?.mPen?.setOnClickListener(this)
        drawBinding?.mEreser?.setOnClickListener(this)
        drawBinding?.mUndu?.setOnClickListener(this)
        drawBinding?.mRedu?.setOnClickListener(this)
        drawBinding?.mDone?.setOnClickListener(this)
        drawBinding?.mRecovery?.setOnTouchListener(this)

        colorList = Util.initColorList()
        mDoodleColor = colorList?.get(0)?.let { resources.getColor(it) }!!
        val listener: ColorAdapter.ColorClickListener = object : ColorAdapter.ColorClickListener {
            override fun onColorClick(position: Int) {
                mDoodleColor =
                    ColorAdapter.colorList?.get(position)?.let { resources.getColor(it) }!!
                selectPen()
            }
        }
        colorAdapter = ColorAdapter(listener, colorList, this@DrawActivity)
        drawBinding?.colorRecycler?.layoutManager =
            LinearLayoutManager(this@DrawActivity, LinearLayoutManager.HORIZONTAL, false)
        drawBinding?.colorRecycler?.layoutAnimation = null
        drawBinding?.colorRecycler?.adapter = colorAdapter
        mPhotoEditor = PhotoEditor.Builder(this, drawBinding?.mImage)
            .setPinchTextScalable(true)
            .setClipSourceImage(false)
            .build()
        mPhotoEditor1 = PhotoEditor.Builder(this, drawBinding?.mImage1)
            .setPinchTextScalable(true)
            .setClipSourceImage(false)
            .build()
        glide(Util.mEditedImageList.get(pos))
        path = File(Util.mEditedImageList.get(pos))
        fileName = path!!.name

        mShapeBuilder = ShapeBuilder()
            .withShapeOpacity(100)
            .withShapeType(ShapeType.BRUSH)
            .withShapeSize(mPenWidth)

        selectPen()

        drawBinding?.mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                drawBinding?.mValue?.text = progress.toString()
                mPenWidth = progress.toFloat()
                selectPen()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        drawBinding?.title?.setOnClickListener { onBackPressed() }
    }

    fun glide(str: String) {
        drawBinding?.mImage?.source.let {
            if (it != null) {
                Glide.with(this@DrawActivity)
                    .load(str)
                    .into(it)
            }
        }
        drawBinding?.mImage1?.source.let {
            if (it != null) {
                Glide.with(this@DrawActivity)
                    .load(str)
                    .into(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mPen -> {
                selectPen()
            }
            R.id.mEreser -> {
                mPhotoEditor?.brushEraser()
                drawBinding?.mPen?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                drawBinding?.mEreser?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.theme_color1
                )
                drawBinding?.mUndu?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                drawBinding?.mRedu?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
            }
            R.id.mUndu -> {
                mPhotoEditor?.undo()
                drawBinding?.mPen?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                drawBinding?.mEreser?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                drawBinding?.mUndu?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.theme_color1
                )
                drawBinding?.mRedu?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
            }
            R.id.mRedu -> {
                mPhotoEditor?.redo()
                drawBinding?.mPen?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                drawBinding?.mEreser?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                drawBinding?.mUndu?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                drawBinding?.mRedu?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.theme_color1
                )
            }
            R.id.mDone -> {
                save()
            }
        }
    }

    fun save() {
        var outputMediaFile: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getExternalFilesDir(Environment.DIRECTORY_DCIM)
            outputMediaFile =
                File(directory.toString() + "/" + System.currentTimeMillis() + fileName)
        } else {
            val folder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + "/.nomedia"
            )
            if (!folder.exists()) folder.mkdirs()
            outputMediaFile =
                File(folder.absolutePath + "/" + System.currentTimeMillis() + fileName)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mPhotoEditor!!.saveAsFile(outputMediaFile.toString(), object : OnSaveListener {
            override fun onSuccess(imagePath: String) {
                Util.mEditedImageList.removeAt(pos)
                Util.mEditedImageList.add(pos, outputMediaFile.absolutePath)
                Util.edited = true

                onBackPressed()
            }

            override fun onFailure(exception: java.lang.Exception) {
                Toast.makeText(this@DrawActivity, "Something went Wrong", Toast.LENGTH_SHORT)
                    .show()
                onBackPressed()
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun selectPen() {
        mPhotoEditor?.setBrushDrawingMode(true)
        mPhotoEditor?.setShape(mShapeBuilder)
        mPhotoEditor?.brushSize = mPenWidth
        mPhotoEditor?.brushColor = mDoodleColor
        drawBinding?.mPen?.backgroundTintList = ContextCompat.getColorStateList(
            applicationContext, R.color.theme_color1
        )
        drawBinding?.mEreser?.backgroundTintList = ContextCompat.getColorStateList(
            applicationContext, R.color.bg_img_transparent
        )
        drawBinding?.mUndu?.backgroundTintList = ContextCompat.getColorStateList(
            applicationContext, R.color.bg_img_transparent
        )
        drawBinding?.mRedu?.backgroundTintList = ContextCompat.getColorStateList(
            applicationContext, R.color.bg_img_transparent
        )
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.mRecovery -> {

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        drawBinding?.mImage?.isVisible = false
                        drawBinding?.mImage1?.isVisible = true
                    }
                    MotionEvent.ACTION_UP -> {
                        drawBinding?.mImage?.isVisible = true
                        drawBinding?.mImage1?.isVisible = false
                    }
                }
            }
        }
        return true
    }


}
