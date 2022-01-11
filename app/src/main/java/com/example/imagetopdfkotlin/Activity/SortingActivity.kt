package com.example.imagetopdfkotlin.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.imagetopdfkotlin.Adapter.SortingImageAdapter
import com.example.imagetopdfkotlin.Interface.CallbackItemTouch
import com.example.imagetopdfkotlin.Interface.MyItemTouchHelperCallback
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.ActivitySortingBinding

class SortingActivity : AppCompatActivity(), CallbackItemTouch {
    var sortBinding: ActivitySortingBinding? = null
    var sortingAdapter: SortingImageAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sortBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sorting)

        sortingAdapter = SortingImageAdapter(
            Util.mEditedImageList,
            this@SortingActivity
        )
        sortBinding?.mImageRec?.layoutManager =
            GridLayoutManager(this@SortingActivity, 2)
        sortBinding?.mImageRec?.layoutAnimation = null
        sortBinding?.mImageRec?.adapter = sortingAdapter

        val callback: ItemTouchHelper.Callback =
            MyItemTouchHelperCallback(this) // create MyItemTouchHelperCallback

        val touchHelper =
            ItemTouchHelper(callback) // Create ItemTouchHelper and pass with parameter the MyItemTouchHelperCallback

        touchHelper.attachToRecyclerView(sortBinding?.mImageRec)

        sortBinding?.title?.setOnClickListener { onBackPressed() }

        sortBinding?.mDone?.setOnClickListener {
            onBackPressed()
        }
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int) {
        Util.mEditedImageList.add(
            newPosition,
            Util.mEditedImageList.removeAt(oldPosition)
        ) // change position
        Util.mNewSelectedImageList.add(
            newPosition,
            Util.mNewSelectedImageList.removeAt(oldPosition)
        )
        sortingAdapter?.notifyDataSetChanged()
    }
}