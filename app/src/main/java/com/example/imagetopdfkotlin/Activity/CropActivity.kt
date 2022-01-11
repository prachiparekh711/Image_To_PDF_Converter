package com.example.imagetopdfkotlin.Activity

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util.Companion.edited
import com.example.imagetopdfkotlin.Utils.Util.Companion.mEditedImageList
import com.example.imagetopdfkotlin.databinding.ActivityCropBinding
import com.isseiaoki.simplecropview.CropImageView
import java.io.*


class CropActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    var cropBinding: ActivityCropBinding? = null
    var pos: Int = 0
    var path: File? = null
    var fileName: String? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_crop)
        cropBinding?.mFree?.setOnClickListener(this)
        cropBinding?.mAutoCrop?.setOnClickListener(this)
        cropBinding?.mFrame11?.setOnClickListener(this)
        cropBinding?.mFrame34?.setOnClickListener(this)
        cropBinding?.mFrame32?.setOnClickListener(this)
        cropBinding?.mFrame169?.setOnClickListener(this)
        cropBinding?.mCrop?.setOnClickListener(this)
        cropBinding?.mRotate?.setOnClickListener(this)
        cropBinding?.mFlip?.setOnClickListener(this)
        cropBinding?.mDone?.setOnClickListener(this)
        cropBinding?.mHorizontal?.setOnClickListener(this)
        cropBinding?.mVertical?.setOnClickListener(this)
        cropBinding?.mRecovery?.setOnTouchListener(this)

        cropBinding?.mCrop?.backgroundTintList =
            ContextCompat.getColorStateList(applicationContext, R.color.theme_color1)
        autoCrop()
        pos = intent.getIntExtra("currentPosition", 0)
        glide(mEditedImageList.get(pos))
        path = File(mEditedImageList.get(pos))
        fileName = path!!.name

        cropBinding?.title?.setOnClickListener { onBackPressed() }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mFree -> {
                runOnUiThread {
                    enableCrop()
                    cropBinding?.mImage?.setCropMode(CropImageView.CropMode.CUSTOM)
                    cropBinding?.mFree?.isSelected = true
                    cropBinding?.mAutoCrop?.isSelected = false
                    cropBinding?.mFrame11?.isSelected = false
                    cropBinding?.mFrame34?.isSelected = false
                    cropBinding?.mFrame32?.isSelected = false
                    cropBinding?.mFrame169?.isSelected = false
                }
            }
            R.id.mAutoCrop -> {
                enableCrop()
                autoCrop()
            }
            R.id.mFrame_1_1 -> {
                runOnUiThread {
                    enableCrop()
                    cropBinding?.mImage?.setCustomRatio(1, 1)
                    cropBinding?.mFree?.isSelected = false
                    cropBinding?.mAutoCrop?.isSelected = false
                    cropBinding?.mFrame11?.isSelected = true
                    cropBinding?.mFrame34?.isSelected = false
                    cropBinding?.mFrame32?.isSelected = false
                    cropBinding?.mFrame169?.isSelected = false

                }
            }
            R.id.mFrame_3_4 -> {
                runOnUiThread {
                    enableCrop()
                    cropBinding?.mImage?.setCustomRatio(3, 4)
                    cropBinding?.mFree?.isSelected = false
                    cropBinding?.mAutoCrop?.isSelected = false
                    cropBinding?.mFrame11?.isSelected = false
                    cropBinding?.mFrame34?.isSelected = true
                    cropBinding?.mFrame32?.isSelected = false
                    cropBinding?.mFrame169?.isSelected = false

                }
            }
            R.id.mFrame_3_2 -> {
                runOnUiThread {
                    enableCrop()
                    cropBinding?.mImage?.setCustomRatio(3, 2)
                    cropBinding?.mFree?.isSelected = false
                    cropBinding?.mAutoCrop?.isSelected = false
                    cropBinding?.mFrame11?.isSelected = false
                    cropBinding?.mFrame34?.isSelected = false
                    cropBinding?.mFrame32?.isSelected = true
                    cropBinding?.mFrame169?.isSelected = false
                }
            }
            R.id.mFrame_16_9 -> {
                runOnUiThread {
                    enableCrop()
                    cropBinding?.mImage?.setCustomRatio(16, 9)
                    cropBinding?.mFree?.isSelected = false
                    cropBinding?.mAutoCrop?.isSelected = false
                    cropBinding?.mFrame11?.isSelected = false
                    cropBinding?.mFrame34?.isSelected = false
                    cropBinding?.mFrame32?.isSelected = false
                    cropBinding?.mFrame169?.isSelected = true
                }
            }
            R.id.mCrop -> {
                cropBinding?.cropLL?.isVisible = true
                cropBinding?.flipLL?.isVisible = false
                enableCrop()
            }
            R.id.mRotate -> {
                cropBinding?.cropLL?.isVisible = true
                cropBinding?.flipLL?.isVisible = false
                cropBinding?.mImage?.setCropEnabled(false)
                cropBinding?.mRotate?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.theme_color1
                )
                cropBinding?.mFlip?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                cropBinding?.mCrop?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                cropBinding?.mImage?.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
            }
            R.id.mFlip -> {
                cropBinding?.cropLL?.isVisible = false
                cropBinding?.flipLL?.isVisible = true
                cropBinding?.mImage?.setCropEnabled(false)
                cropBinding?.mRotate?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
                cropBinding?.mFlip?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.theme_color1
                )
                cropBinding?.mCrop?.backgroundTintList = ContextCompat.getColorStateList(
                    applicationContext, R.color.bg_img_transparent
                )
            }
            R.id.mHorizontal -> {
                cropBinding?.mHorizontal?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color1
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                cropBinding?.mVertical?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_btn
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                val flipBitmap = cropBinding?.mImage?.imageBitmap?.let { flipImage(it, 1) }
                cropBinding?.mImage?.let {
                    Glide.with(this@CropActivity)
                        .load(flipBitmap)
                        .into(it)
                }
            }
            R.id.mVertical -> {
                cropBinding?.mHorizontal?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_btn
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                cropBinding?.mVertical?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.theme_color1
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                val flipBitmap = cropBinding?.mImage?.imageBitmap?.let { flipImage(it, 0) }
                cropBinding?.mImage?.let {
                    Glide.with(this@CropActivity)
                        .load(flipBitmap)
                        .into(it)
                }
            }
            R.id.mDone -> {
                var outputMediaFile: File? = null
                if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

                try {
                    val out = FileOutputStream(outputMediaFile)
                    cropBinding?.mImage?.croppedBitmap?.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        out
                    )
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mEditedImageList.removeAt(pos)
                mEditedImageList.add(pos, outputMediaFile.absolutePath)
                edited = true

                onBackPressed()
            }
        }
    }


    fun flipImage(src: Bitmap, type: Int): Bitmap? {
        val matrix = Matrix()
        if (type == 0) {
            matrix.preScale(1.0f, -1.0f)
        } else if (type == 1) {
            matrix.preScale(-1.0f, 1.0f)
        } else {
            return null
        }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun autoCrop() {
        runOnUiThread {
            cropBinding?.mImage?.setCropMode(CropImageView.CropMode.CUSTOM)
            cropBinding?.mFree?.isSelected = false
            cropBinding?.mAutoCrop?.isSelected = true
            cropBinding?.mFrame11?.isSelected = false
            cropBinding?.mFrame34?.isSelected = false
            cropBinding?.mFrame32?.isSelected = false
            cropBinding?.mFrame169?.isSelected = false
        }
    }

    fun glide(str: String) {
        cropBinding?.mImage?.let {
            Glide.with(this@CropActivity)
                .load(str)
                .into(it)
        }
        cropBinding?.mImage1?.let {
            Glide.with(this@CropActivity)
                .load(str)
                .into(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun enableCrop() {
        cropBinding?.mImage?.setCropEnabled(true)
        cropBinding?.mRotate?.backgroundTintList = ContextCompat.getColorStateList(
            applicationContext, R.color.bg_img_transparent
        )
        cropBinding?.mFlip?.backgroundTintList =
            ContextCompat.getColorStateList(applicationContext, R.color.bg_img_transparent)
        cropBinding?.mCrop?.backgroundTintList =
            ContextCompat.getColorStateList(applicationContext, R.color.theme_color1)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.mRecovery -> {
                cropBinding?.mImage1?.setCropEnabled(false)
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        cropBinding?.mImage?.isVisible = false
                        cropBinding?.mImage1?.isVisible = true
                    }
                    MotionEvent.ACTION_UP -> {
                        cropBinding?.mImage?.isVisible = true
                        cropBinding?.mImage1?.isVisible = false
                    }
                }
            }
        }
        return true
    }
}
