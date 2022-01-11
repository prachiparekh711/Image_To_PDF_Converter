package com.example.imagetopdfkotlin.Activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imagetopdfkotlin.Adapter.BottomImageAdapter
import com.example.imagetopdfkotlin.Adapter.ImagesAdapter
import com.example.imagetopdfkotlin.Adapter.ShowImageAdapter
import com.example.imagetopdfkotlin.Model.BaseModel
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.ActivityInnerFolderBinding
import com.yarolegovich.discretescrollview.DiscreteScrollView
import es.dmoral.toasty.Toasty
import java.util.*


class InnerFolderActivity : AppCompatActivity(),
    DiscreteScrollView.ScrollStateChangeListener<ShowImageAdapter.MyClassView> {

    var baseModel: BaseModel? = null
    private var bottomReceiver: BottomReceiver? = null
    private var topReceiver: TopReceiver? = null
    private var imageReceiver: ImageReceiver? = null


    companion object {
        var mActivity: Activity? = null
        var mImageList: ArrayList<String>? = null
        var imagesAdapter: ImagesAdapter? = null
        var showImagesAdapter: ShowImageAdapter? = null
        var bottomImagesAdapter: BottomImageAdapter? = null
        var innerFolderBinding: ActivityInnerFolderBinding? = null
        var currentPosition: Int = 0
        fun bottomView() {
            if (Util.mSelectedImageList != null && Util.mSelectedImageList.size > 0) {
                innerFolderBinding?.bottomRL?.isVisible = true

                bottomImagesAdapter?.newArray()
                for (i in Util.mSelectedImageList.indices) {
                    bottomImagesAdapter?.add(i, Util.mSelectedImageList[i])
                }

                innerFolderBinding?.mImageRec?.scrollToPosition(Util.mSelectedImageList.size - 1)
            } else {
                innerFolderBinding?.bottomRL?.isGone = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        innerFolderBinding =
            DataBindingUtil.setContentView(this@InnerFolderActivity, R.layout.activity_inner_folder)

        mActivity = this@InnerFolderActivity
        bottomReceiver = BottomReceiver()
        LocalBroadcastManager.getInstance(this@InnerFolderActivity).registerReceiver(
            bottomReceiver!!,
            IntentFilter("CHANGE_BOTTOM")
        )

        topReceiver = TopReceiver()
        LocalBroadcastManager.getInstance(this@InnerFolderActivity).registerReceiver(
            topReceiver!!,
            IntentFilter("CHANGE_TOP")
        )

        imageReceiver = ImageReceiver()
        LocalBroadcastManager.getInstance(this@InnerFolderActivity).registerReceiver(
            imageReceiver!!,
            IntentFilter("OPEN_IMAGE")
        )

        baseModel = intent.getSerializableExtra("baseModel") as? BaseModel

        mImageList = baseModel?.pathlist
        if (mImageList != null && mImageList?.size!! > 0) {
            imagesAdapter =
                ImagesAdapter(this@InnerFolderActivity)
            imagesAdapter?.mArrayList?.clear()
            for (i in mImageList!!.indices) {
                imagesAdapter?.add(i, mImageList!![i])
            }

            innerFolderBinding?.folderRec?.layoutManager =
                GridLayoutManager(this@InnerFolderActivity.baseContext, 2)
            innerFolderBinding?.folderRec?.layoutAnimation = null
            innerFolderBinding?.folderRec?.adapter = imagesAdapter

            showImagesAdapter = ShowImageAdapter(this@InnerFolderActivity)
            for (i in mImageList!!.indices) {
                showImagesAdapter?.add(i, mImageList!![i])
            }
            innerFolderBinding?.picker?.adapter = showImagesAdapter
            innerFolderBinding?.picker?.setOverScrollEnabled(true)
            innerFolderBinding?.picker?.setSlideOnFling(true)
        }

        bottomImagesAdapter =
            BottomImageAdapter(this@InnerFolderActivity, "InnerFolderActivity")
        innerFolderBinding?.mImageRec?.layoutManager =
            LinearLayoutManager(this@InnerFolderActivity, LinearLayoutManager.HORIZONTAL, false)
        innerFolderBinding?.mImageRec?.layoutAnimation = null
        innerFolderBinding?.mImageRec?.adapter = bottomImagesAdapter
        if (Util.mSelectedImageList != null && Util.mSelectedImageList.size > 0) {
            innerFolderBinding?.mImportText?.text =
                "Import (".plus(Util.mSelectedImageList.size).plus(")")
            for (i in Util.mSelectedImageList.indices) {
                bottomImagesAdapter?.add(i, Util.mSelectedImageList[i])
            }
            innerFolderBinding?.bottomRL?.isVisible = true
        } else {
            innerFolderBinding?.bottomRL?.isGone = true
        }

        innerFolderBinding?.title?.setOnClickListener { onBackPressed() }

        innerFolderBinding?.picker?.addScrollStateChangeListener(this)

        innerFolderBinding?.mCheckbox?.setOnClickListener {

            val path: String? = mImageList?.get(currentPosition)
            if (path in Util.mSelectedImageList) {
                Util.mSelectedImageList.remove(path)
                Util.mNewSelectedImageList.remove(path)
                innerFolderBinding?.mCheckbox?.isSelected = false
            } else {
                if (path != null) {
                    Util.mSelectedImageList.add(path)
                    Util.mNewSelectedImageList.add(path)
                }
                innerFolderBinding?.mCheckbox?.isSelected = true
            }

            innerFolderBinding?.mImportText?.text =
                "Import (".plus(Util.mSelectedImageList.size).plus(")")
            bottomView()
            imagesAdapter?.notifyDataSetChanged()
        }

        innerFolderBinding?.mImport?.setOnClickListener {
            if (Util.mSelectedImageList.size > 0) {
                val intent = Intent(this, SelectedImageActivity::class.java)
                startActivity(intent)
            } else {
                Toasty.info(this@InnerFolderActivity, "Nothing selected").show()
            }
        }
    }

    private class BottomReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            bottomView()
        }
    }

    private class TopReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (Util.mSelectedImageList != null && Util.mSelectedImageList.size > 0) {
                innerFolderBinding?.bottomRL?.isVisible = true
            } else {
                innerFolderBinding?.bottomRL?.isGone = true
            }

            mActivity?.runOnUiThread { imagesAdapter?.notifyDataSetChanged() }

        }
    }

    private class ImageReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val pos = intent?.getIntExtra("pos", 0)
            if (pos != null) {
                Log.e("pos:", pos.toString())
                currentPosition = pos
            }
            innerFolderBinding?.folderRec?.isGone = true
            innerFolderBinding?.picker?.isVisible = true
            innerFolderBinding?.mTopCheckbox?.isVisible = true
            if (pos != null) {
                innerFolderBinding?.picker?.scrollToPosition(pos)
                innerFolderBinding?.mCheckbox?.isSelected =
                    Util.mSelectedImageList.contains(mImageList?.get(pos))
                val str = (pos + 1).toString().plus("/" + mImageList?.size)
                innerFolderBinding?.title?.text = str
            }
        }
    }

    override fun onBackPressed() {
        if (innerFolderBinding?.picker?.isVisible == true) {
            innerFolderBinding?.folderRec?.isVisible = true
            innerFolderBinding?.picker?.isGone = true
            innerFolderBinding?.mTopCheckbox?.isGone = true
            innerFolderBinding?.title?.text = resources.getString(R.string.select_img)
        } else {
            super.onBackPressed()
        }
    }

    override fun onScrollStart(
        currentItemHolder: ShowImageAdapter.MyClassView,
        adapterPosition: Int
    ) {
    }

    override fun onScrollEnd(
        currentItemHolder: ShowImageAdapter.MyClassView,
        adapterPosition: Int
    ) {
        currentPosition = adapterPosition
        val str = (adapterPosition + 1).toString().plus("/" + mImageList?.size)
        innerFolderBinding?.title?.text = str

        innerFolderBinding?.mCheckbox?.isSelected =
            Util.mSelectedImageList.contains(mImageList?.get(adapterPosition))
    }

    override fun onScroll(
        scrollPosition: Float,
        currentPosition: Int,
        newPosition: Int,
        currentHolder: ShowImageAdapter.MyClassView?,
        newCurrent: ShowImageAdapter.MyClassView?
    ) {

    }

}
