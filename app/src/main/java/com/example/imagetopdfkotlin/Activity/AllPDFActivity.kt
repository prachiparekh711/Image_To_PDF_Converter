package com.example.imagetopdfkotlin.Activity

import android.app.Activity
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetopdfkotlin.Adapter.DataAdapter
import com.example.imagetopdfkotlin.Fragment.RecentFragment
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.ActivityAllPdfactivityBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class AllPDFActivity : AppCompatActivity(), View.OnClickListener {


    private var moreReceiver: MoreAllReceiver? = null
    private var refreshReceiver: RefreshReceiver? = null

    companion object {
        var file: File? = null
        lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
        var mainBinding: ActivityAllPdfactivityBinding? = null
        var exist = false
        var pos = 0
        var gson: Gson? = null
        var sharedPreferences: SharedPreferences? = null
        var editor: SharedPreferences.Editor? = null
        var mFavouriteImageList = ArrayList<String>()
        var mActivity: Activity? = null
        var dataAdapter: DataAdapter? = null
        var generatedPDFList = ArrayList<String>()

        fun fetchData() {
            dataAdapter?.newArray(2)
            val folder: File
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cw = ContextWrapper(RecentFragment.mActivity?.applicationContext)
                folder = File(
                    cw.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                    RecentFragment.mActivity?.resources?.getString(R.string.app_name)
                )
            } else {
                folder = File(
                    Util.BASE_PATH,
                    RecentFragment.mActivity?.resources?.getString(R.string.app_name)
                )
            }
            generatedPDFList.clear()
            if (folder.exists()) {
                val files = folder.listFiles()
                for (i in files.indices) {
                    val file = File(files[i].path)
                    if (file.exists()) {
                        generatedPDFList.add(files[i].path)
                    }
                }
                Collections.sort(generatedPDFList, object : Comparator<String?> {
                    override fun compare(t1: String?, t2: String?): Int {
                        val file1 = File(t1)
                        val file2 = File(t2)
                        return java.lang.Long.compare(file1.lastModified(), file2.lastModified())
                    }
                })
                if (generatedPDFList.size > 0) {
                    mainBinding?.mImageRec?.isVisible = true
                    mainBinding?.mNoData?.isVisible = false
                    for (i in generatedPDFList.indices) {
                        dataAdapter?.add(i, generatedPDFList.get(i))
                    }
                } else {
                    mainBinding?.mImageRec?.isVisible = false
                    mainBinding?.mNoData?.isVisible = true
                }
            }
        }
    }

    private class RefreshReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            fetchData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding =
            DataBindingUtil.setContentView(this@AllPDFActivity, R.layout.activity_all_pdfactivity)
        mActivity = this@AllPDFActivity
        bottomSheetBehavior = BottomSheetBehavior.from(bottomOption)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mainBinding?.blankRL?.isVisible = false
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
        moreReceiver = MoreAllReceiver()
        LocalBroadcastManager.getInstance(this@AllPDFActivity).registerReceiver(
            moreReceiver!!,
            IntentFilter("MORE_ALL")
        )

        refreshReceiver = RefreshReceiver()
        LocalBroadcastManager.getInstance(this@AllPDFActivity).registerReceiver(
            refreshReceiver!!,
            IntentFilter("REFRESH")
        )

        dataAdapter =
            DataAdapter(this@AllPDFActivity)
        mainBinding?.mImageRec?.layoutManager =
            LinearLayoutManager(this@AllPDFActivity, RecyclerView.VERTICAL, false)
        mainBinding?.mImageRec?.layoutAnimation = null
        mainBinding?.mImageRec?.adapter = dataAdapter

        fetchData()

        mCloud?.setOnClickListener(this)
        mPrinter?.setOnClickListener(this)
        mBookmark?.setOnClickListener(this)
        mSetPassword?.setOnClickListener(this)
        mRename?.setOnClickListener(this)
        mDelete?.setOnClickListener(this)
    }


    private class MoreAllReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            file = File(intent?.getStringExtra("file"))
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                mainBinding?.blankRL?.isVisible = false
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                mainBinding?.blankRL?.isVisible = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            mainBinding?.pdfName?.text = file?.name
            sharedPreferences = mActivity?.getSharedPreferences("Favourites_pref", MODE_PRIVATE)
            editor = sharedPreferences?.edit()
            try {
                gson = Gson()
                val json = sharedPreferences?.getString("Fav_Image", "")
                val type = object : TypeToken<ArrayList<String?>?>() {}.type
                mFavouriteImageList = gson?.fromJson(json, type)!!

                if (mFavouriteImageList.size > 0) {
                    if (mFavouriteImageList.contains(file?.path)) {
                        mainBinding?.mBookmark?.text = "Bookmarked"
                    } else {
                        mainBinding?.mBookmark?.text = "Add To Bookmark"
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Log.e("error", it) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mCloud -> {

            }
            R.id.mPrinter -> {
                var print = false
                file?.path?.let { print = Util.printPDF(it, baseContext) }
                if (!print) {
                    Toasty.info(baseContext, "Can't print password protected PDF.").show()
                }
            }
            R.id.mBookmark -> {
                file?.let { Util.bookmark(it, baseContext) }
            }
            R.id.mSetPassword -> {
                file?.let { Util.addPassword(it, this@AllPDFActivity) }
            }
            R.id.mRename -> {
                file?.let {
                    Util.rename(it, this@AllPDFActivity)
                    runOnUiThread { fetchData() }
                }
            }
            R.id.mDelete -> {
                file?.let { Util.delete(it, this@AllPDFActivity) }
                runOnUiThread { fetchData() }
            }
        }
        mainBinding?.blankRL?.isVisible = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

}