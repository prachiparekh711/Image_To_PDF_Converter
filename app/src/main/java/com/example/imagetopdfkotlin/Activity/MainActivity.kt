package com.example.imagetopdfkotlin.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.imagetopdfkotlin.Adapter.LibraryPagerAdapter
import com.example.imagetopdfkotlin.Fragment.BookmarkFragment
import com.example.imagetopdfkotlin.Fragment.HomeFragment
import com.example.imagetopdfkotlin.Fragment.RecentFragment
import com.example.imagetopdfkotlin.Fragment.SettingFragment
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_selected_image.*
import java.io.File
import java.util.*

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    var mViewPagerAdapter: LibraryPagerAdapter? = null
    var prevMenuItem: MenuItem? = null
    private var moreReceiver: MoreReceiver? = null
    var json1: String? = null
    var addPassword = false
    var mPassword: String? = null

    companion object {
        var file: File? = null
        var from: String? = null
        lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
        var mainBinding: ActivityMainBinding? = null
        var mActivity: Activity? = null
        var isPasswordProtected = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        mainBinding?.bottomNavigationView?.itemIconTintList = null
        mainBinding?.bottomNavigationView?.setOnNavigationItemSelectedListener(this)
        mActivity = this@MainActivity

        mViewPagerAdapter = LibraryPagerAdapter(supportFragmentManager)
        mainBinding?.viewPager?.adapter = mViewPagerAdapter
        mainBinding?.viewPager?.offscreenPageLimit = 4

        val from = intent.getStringExtra("from")
        if (!from.equals("add_more")) {
            Util.mSelectedImageList.clear()
            Util.mNewSelectedImageList.clear()
            Util.mEditedImageList.clear()
        }

        mainBinding?.viewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem?.isChecked = false
                } else {
                    mainBinding?.bottomNavigationView?.menu?.getItem(0)?.isChecked = false
                }
                mainBinding?.bottomNavigationView?.menu?.getItem(i)?.isChecked = true
                prevMenuItem = mainBinding?.bottomNavigationView?.menu?.getItem(i)
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })

        bottomSheetBehavior = BottomSheetBehavior.from(bottomOption)
        moreReceiver = MoreReceiver()
        LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(
            moreReceiver!!,
            IntentFilter("MORE")
        )

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

        mCloud?.setOnClickListener(this)
        mPrinter?.setOnClickListener(this)
        mBookmark?.setOnClickListener(this)
        mSetPassword?.setOnClickListener(this)
        mRename?.setOnClickListener(this)
        mDelete?.setOnClickListener(this)
    }

    private class MoreReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            file = File(intent?.getStringExtra("file"))
            from = intent?.getStringExtra("from")
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                mainBinding?.blankRL?.isVisible = false
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                mainBinding?.blankRL?.isVisible = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            mainBinding?.pdfName?.text = file?.name
            Util.sharedPreferences =
                mActivity?.getSharedPreferences("Favourites_pref", MODE_PRIVATE)
            Util.editor = Util.sharedPreferences?.edit()
            try {
                Util.gson = Gson()
                val json = Util.sharedPreferences?.getString("Fav_Image", "")
                val type = object : TypeToken<ArrayList<String?>?>() {}.type
                Util.mFavouriteImageList = Util.gson?.fromJson(json, type)!!

                if (Util.mFavouriteImageList.size > 0) {
                    if (Util.mFavouriteImageList.contains(file?.path)) {
                        mainBinding?.mBookmark?.text = "Bookmarked"
                    } else {
                        mainBinding?.mBookmark?.text = "Add To Bookmark"
                    }

                    if (Util.IsPasswordProtected(file?.path)) {
                        isPasswordProtected = true
                        mainBinding?.mSetPassword?.text = "Remove Password"
                    } else {
                        isPasswordProtected = false
                        mainBinding?.mSetPassword?.text = "Set Password"
                    }
                }


            } catch (e: Exception) {
                e.message?.let { Log.e("error", it) }
            }

        }
    }

    override fun permissionGranted() {

        mainBinding?.viewPager?.let { setupViewPager(it) }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                mainBinding?.viewPager?.currentItem = 0
            }
            R.id.recent -> {
                mainBinding?.viewPager?.currentItem = 1
            }
            R.id.fav -> {
                mainBinding?.viewPager?.currentItem = 2
            }
            R.id.setting -> {
                mainBinding?.viewPager?.currentItem = 3
            }
        }
        return false
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = LibraryPagerAdapter(supportFragmentManager)
        val mainFragment = HomeFragment()
        val recentFragment = RecentFragment()
        val bookmarkFragment = BookmarkFragment()
        val settingFragment = SettingFragment()
        adapter.addFragment(mainFragment)
        adapter.addFragment(recentFragment)
        adapter.addFragment(bookmarkFragment)
        adapter.addFragment(settingFragment)
        viewPager.adapter = adapter
    }

    override fun onBackPressed() {
        if (mainBinding?.viewPager?.currentItem != 0) {
            mainBinding?.viewPager?.currentItem = 0
        } else {
            finishAffinity()
            super.onBackPressed()
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
                file?.let {
                    Util.bookmark(it, baseContext)
                    val lbm = LocalBroadcastManager.getInstance(this@MainActivity)
                    val localIn = Intent("REFRESH")
                    lbm.sendBroadcast(localIn)
                }
            }
            R.id.mSetPassword -> {
                if (isPasswordProtected) {
                    file?.let { Util.removePassword(it, this@MainActivity) }
                } else {
                    file?.let { Util.addPassword(it, this@MainActivity) }
                }
            }
            R.id.mRename -> {
                file?.path?.let { Log.e("file", it) }
                file?.let { Util.rename(it, this@MainActivity) }
            }
            R.id.mDelete -> {
                if (from.equals("BOOKMARK")) {
                    file?.let { Util.bookmark(it, this@MainActivity) }
                } else {
                    file?.let { Util.delete(it, this@MainActivity) }
                }
                val lbm = LocalBroadcastManager.getInstance(this@MainActivity)
                val localIn = Intent("REFRESH")
                lbm.sendBroadcast(localIn)
            }
        }
        mainBinding?.blankRL?.isVisible = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun rename(file: File, activity: Activity) {
        val alertadd = AlertDialog.Builder(activity).create()
        val factory = LayoutInflater.from(activity)
        val view = factory.inflate(R.layout.dialog, null)
        alertadd.setView(view)
        alertadd.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertadd.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val renameCard: CardView = view.findViewById(R.id.renameRL)
        renameCard.visibility = View.VISIBLE
        val cancelIcon = view.findViewById<ImageView>(R.id.cancelIcon)
        val newName = view.findViewById<EditText>(R.id.etName)
        val cancel: CardView = view.findViewById(R.id.cancel)
        val rename: CardView = view.findViewById(R.id.rename)
        var fileName = file.name
        val pos = fileName.lastIndexOf(".")
        if (pos > 0 && pos < file.name.length - 1) {
            fileName = fileName.substring(0, pos)
        }
        newName.hint = fileName
        newName.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Util.hideKeyboard(newName, activity)
                return@setOnEditorActionListener true
            }
            false
        }
        cancel.setOnClickListener { alertadd.dismiss() }
        cancelIcon.setOnClickListener { newName.setText("") }
        rename.setOnClickListener {
            val name = newName.text.toString()
            if (name != "") {
                val extension = file.absolutePath.substring(file.absolutePath.lastIndexOf("."))
                val FolderPath = file.parentFile.absolutePath
                val to = File("$FolderPath/$name$extension")
                val isRename = file.renameTo(to)
                if (!isRename) {
                    Toasty.error(activity, "Something went wrong!!!").show()
                } else {
                    Util.sharedPreferences =
                        getSharedPreferences(
                            "Favourites_pref",
                            MODE_PRIVATE
                        )
                    Util.editor = Util.sharedPreferences?.edit()
                    try {
                        Util.gson = Gson()
                        val json = Util.sharedPreferences?.getString("Fav_Image", "")
                        val type = object : TypeToken<ArrayList<String?>?>() {}.type
                        Util.mFavouriteImageList = Util.gson?.fromJson(json, type)!!

                        if (Util.mFavouriteImageList.size > 0) {
                            if (Util.mFavouriteImageList.contains(file.path)) {
                                Util.mFavouriteImageList.remove(file.path)
                                Util.mFavouriteImageList.add(to.path)
                                Util.json1 = Util.gson?.toJson(Util.mFavouriteImageList)
                                Util.editor?.putString("Fav_Image", Util.json1)
                                Util.editor?.apply()
                            }
                        }
                    } catch (e: Exception) {
                        e.message?.let { Log.e("error", it) }
                    }

                    Toasty.success(activity, "Rename successfully !").show()
                    val lbm = LocalBroadcastManager.getInstance(activity)
                    val localIn = Intent("REFRESH")
                    lbm.sendBroadcast(localIn)
                }
                alertadd.dismiss()
            }
        }
        alertadd.show()
    }


}
