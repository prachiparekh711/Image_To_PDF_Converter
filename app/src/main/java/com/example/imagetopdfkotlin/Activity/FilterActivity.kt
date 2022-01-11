package com.example.imagetopdfkotlin.Activity

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.imagetopdfkotlin.Fragment.FilterListFragment
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.ActivityFilterBinding
import com.zomato.photofilters.imageprocessors.Filter
import java.io.File
import java.io.FileOutputStream
import java.util.*

class FilterActivity : AppCompatActivity(), FilterListFragment.FiltersListFragmentListener,
    View.OnClickListener, View.OnTouchListener {

    companion object {
        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }

    var filtersListFragment: FilterListFragment? = null
    var filterBinding: ActivityFilterBinding? = null
    var mFilterBitmap: Bitmap? = null
    var pos: Int = 0
    var path: File? = null
    var fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filterBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_filter)

        pos = intent.getIntExtra("currentPosition", 0)
        glide(Util.mEditedImageList.get(pos))
        path = File(Util.mEditedImageList.get(pos))
        fileName = path!!.name
        mFilterBitmap = BitmapFromPath(Util.mEditedImageList.get(pos))
        Util.editedBitmap = BitmapFromPath(Util.mEditedImageList.get(pos))

        val adapter = ViewPagerAdapter(supportFragmentManager)

        filterBinding?.mDone?.setOnClickListener(this)
        filterBinding?.mRecovery?.setOnTouchListener(this)

        filtersListFragment = FilterListFragment()
        filtersListFragment?.listener = this

        adapter.addFragment(filtersListFragment!!, "Filters")

        filterBinding?.filterPager?.adapter = adapter
    }

    override fun onFilterSelected(filter: Filter?) {
        var bitmap: Bitmap? = mFilterBitmap
        val maxSize = 960
        var outWidth: Int = 0
        var outHeight: Int = 0
        val inWidth = bitmap?.width
        val inHeight = bitmap?.height
        if (inWidth != null) {
            if (inWidth > inHeight!!) {
                outWidth = maxSize
                outHeight = inHeight * maxSize / inWidth
            } else {
                outHeight = maxSize
                outWidth = inWidth * maxSize / inHeight
            }
        }
        bitmap = bitmap?.let { Bitmap.createScaledBitmap(it, outWidth, outHeight, true) }

        val bitmap1 = filter!!.processFilter(bitmap)
        filterBinding?.mImage?.setImageBitmap(bitmap1)
        Util.editedBitmap = bitmap1
    }

    internal class ViewPagerAdapter(manager: FragmentManager?) :
        FragmentPagerAdapter(manager!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    fun glide(str: String) {
        val d = Drawable.createFromPath(str)
        filterBinding?.mImage?.setImageDrawable(d)
        filterBinding?.mImage1?.setImageDrawable(d)
    }

    fun BitmapFromPath(path: String?): Bitmap? {
        val bmOptions = BitmapFactory.Options()
        return BitmapFactory.decodeFile(path, bmOptions)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.mDone -> {
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

                try {
                    val out = FileOutputStream(outputMediaFile)
                    Util.editedBitmap?.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        out
                    )
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Util.mEditedImageList.removeAt(pos)
                Util.mEditedImageList.add(pos, outputMediaFile.absolutePath)
                Util.edited = true

                onBackPressed()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            R.id.mRecovery -> {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        filterBinding?.mImage?.isVisible = false
                        filterBinding?.mImage1?.isVisible = true
                    }
                    MotionEvent.ACTION_UP -> {
                        filterBinding?.mImage?.isVisible = true
                        filterBinding?.mImage1?.isVisible = false
                    }
                }
            }
        }
        return true
    }
}