package com.example.imagetopdfkotlin.Activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.imagetopdfkotlin.Adapter.BottomImageAdapter
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.Utils.Util.Companion.edited
import com.example.imagetopdfkotlin.Utils.Util.Companion.finalEdit
import com.example.imagetopdfkotlin.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    var bottomImagesAdapter: BottomImageAdapter? = null
    private var imageReceiver: ImageReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_edit)
        activity = this@EditActivity

        editBinding?.title?.setOnClickListener { onBackPressed() }
        currentPosition = 0
        editBinding?.mDone?.setOnClickListener(this)
        editBinding?.mRecovery?.setOnTouchListener(this)

        editBinding?.mEdit?.setOnClickListener {
            val intent = Intent(this, CropActivity::class.java)
            intent.putExtra("currentPosition", currentPosition)
            startActivity(intent)
        }

        editBinding?.mDraw?.setOnClickListener {
            val intent = Intent(this, DrawActivity::class.java)
            intent.putExtra("currentPosition", currentPosition)
            startActivity(intent)
        }

        editBinding?.mFilter?.setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java)
            intent.putExtra("currentPosition", currentPosition)
            startActivity(intent)
        }

        imageReceiver = ImageReceiver()
        LocalBroadcastManager.getInstance(this@EditActivity).registerReceiver(
            imageReceiver!!,
            IntentFilter("CHANGE_IMAGE")
        )
        bottomImagesAdapter =
            BottomImageAdapter(this@EditActivity, "EditActivity")
        editBinding?.mImageRec?.layoutManager =
            LinearLayoutManager(this@EditActivity, LinearLayoutManager.HORIZONTAL, false)
        editBinding?.mImageRec?.layoutAnimation = null
        editBinding?.mImageRec?.adapter =
            bottomImagesAdapter
        Util.mEditedImageList.clear()
        if (Util.mNewSelectedImageList.size > 0) {
            for (i in Util.mNewSelectedImageList.indices) {
                bottomImagesAdapter?.add(i, Util.mNewSelectedImageList[i])
                Util.mEditedImageList.add(Util.mNewSelectedImageList[i])
            }
        }
        displayImg(0)

    }

    private class ImageReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            currentPosition = intent?.getIntExtra("pos", 0)
            currentPosition?.let { displayImg(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        if (edited) {
            edited = false
            editBinding?.mImage?.let {
                activity?.let { it1 ->
                    Glide.with(it1)
                        .load(Util.mEditedImageList[currentPosition!!])
                        .into(it)
                }
            }
            bottomImagesAdapter?.newArray()
            editBinding?.mImageRec?.adapter =
                bottomImagesAdapter
            if (Util.mEditedImageList.size > 0) {
                for (i in Util.mEditedImageList.indices) {
                    bottomImagesAdapter?.add(i, Util.mEditedImageList[i])
                }
            }
        }
    }

    companion object {
        var currentPosition: Int? = 0
        var editBinding: ActivityEditBinding? = null
        var activity: Activity? = null
        fun displayImg(pos: Int) {
            editBinding?.mImage?.let {
                activity?.let { it1 ->
                    Glide.with(it1)
                        .load(Util.mEditedImageList.get(pos))
                        .into(it)
                }
            }
            editBinding?.mImage1?.let {
                activity?.let { it1 ->
                    Glide.with(it1)
                        .load(Util.mEditedImageList.get(pos))
                        .into(it)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.mDone -> {
                finalEdit = true
                onBackPressed()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.mRecovery -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        editBinding?.mImage?.isVisible = false
                        editBinding?.mImage1?.isVisible = true
                    }
                    MotionEvent.ACTION_UP -> {
                        editBinding?.mImage?.isVisible = true
                        editBinding?.mImage1?.isVisible = false
                    }
                }
            }
        }
        return true
    }
}