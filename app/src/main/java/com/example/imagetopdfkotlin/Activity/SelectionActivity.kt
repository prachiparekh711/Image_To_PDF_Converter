package com.example.imagetopdfkotlin.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagetopdfkotlin.Adapter.SelectedImageAdapter
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {

    var selectionBinding: ActivitySelectionBinding? = null
    var selecteImageAdapter: SelectedImageAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectionBinding =
            DataBindingUtil.setContentView(this@SelectionActivity, R.layout.activity_selection)

        selecteImageAdapter =
            SelectedImageAdapter(this@SelectionActivity)
        selectionBinding?.mImageRec?.layoutManager =
            GridLayoutManager(this@SelectionActivity, 2)
        selectionBinding?.mImageRec?.layoutAnimation = null
        selectionBinding?.mImageRec?.adapter = selecteImageAdapter
        if (Util.mNewSelectedImageList != null && Util.mNewSelectedImageList.size > 0) {
            selecteImageAdapter?.newArray(1)
            for (i in Util.mNewSelectedImageList.indices) {
                selecteImageAdapter?.add(i, Util.mNewSelectedImageList[i])
            }
        }

        selectionBinding?.title?.setOnClickListener { onBackPressed() }

        selectionBinding?.mDelete?.setOnClickListener {
            Util.mEditedImageList.clear()
            Util.mNewSelectedImageList.clear()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}