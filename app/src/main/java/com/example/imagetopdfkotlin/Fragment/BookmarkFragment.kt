package com.example.imagetopdfkotlin.Fragment

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetopdfkotlin.Adapter.DataAdapter
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.databinding.FragmentBookmarkBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BookmarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookmarkFragment : Fragment() {
    private var refreshReceiver: RefreshReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bookmarkBinding =
            DataBindingUtil.inflate(
                inflater, R.layout.fragment_bookmark, container, false
            )
        view1 = bookmarkBinding?.root
        mActivity = requireActivity()
        dataAdapter =
            DataAdapter(requireActivity())
        bookmarkBinding?.mImageRec?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        bookmarkBinding?.mImageRec?.layoutAnimation = null
        bookmarkBinding?.mImageRec?.adapter = dataAdapter

        refreshReceiver = RefreshReceiver()
        LocalBroadcastManager.getInstance(requireActivity().baseContext).registerReceiver(
            refreshReceiver!!,
            IntentFilter("REFRESH")
        )

        return view1
    }

    companion object {
        var bookmarkBinding: FragmentBookmarkBinding? = null
        var dataAdapter: DataAdapter? = null
        var mFavouriteImageList = ArrayList<String>()
        var json1: String? = null
        var sharedPreferences: SharedPreferences? = null
        var gson: Gson? = null
        var view1: View? = null
        var mActivity: FragmentActivity? = null

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookmarkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun setAdapter() {
            dataAdapter?.newArray(0)

            mFavouriteImageList = ArrayList()
            sharedPreferences =
                mActivity?.getSharedPreferences("Favourites_pref", Context.MODE_PRIVATE)
            gson = Gson()
            json1 = sharedPreferences!!.getString("Fav_Image", "")
            val type1 = object : TypeToken<ArrayList<String?>?>() {}.type
            try {
                mFavouriteImageList = gson!!.fromJson(json1, type1)

                if (mFavouriteImageList.size > 0) {
                    bookmarkBinding?.mImageRec?.isVisible = true
                    bookmarkBinding?.mNoData?.isVisible = false
                    for (i in mFavouriteImageList.indices) {
                        val file = File(mFavouriteImageList[i])
                        if (file.exists()) {
                            dataAdapter?.add(i, mFavouriteImageList[i])
                        }
                    }
                } else {
                    bookmarkBinding?.mImageRec?.isVisible = false
                    bookmarkBinding?.mNoData?.isVisible = true
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private class RefreshReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            setAdapter()
        }
    }

}