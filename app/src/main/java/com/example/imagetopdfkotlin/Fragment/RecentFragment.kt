package com.example.imagetopdfkotlin.Fragment

import android.app.Activity
import android.content.*
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
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
import com.example.imagetopdfkotlin.Utils.Util.Companion.BASE_PATH
import com.example.imagetopdfkotlin.databinding.FragmentRecentBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecentFragment : Fragment() {

    var view1: View? = null
    private var refreshReceiver: RefreshReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recentBinding =
            DataBindingUtil.inflate(
                inflater, R.layout.fragment_recent, container, false
            )
        view1 = recentBinding?.root
        mActivity = requireActivity()
        dataAdapter =
            DataAdapter(requireActivity())
        recentBinding?.mImageRec?.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        recentBinding?.mImageRec?.layoutAnimation = null
        recentBinding?.mImageRec?.adapter = dataAdapter

        refreshReceiver = RefreshReceiver()
        LocalBroadcastManager.getInstance((mActivity as FragmentActivity).baseContext)
            .registerReceiver(
                refreshReceiver!!,
                IntentFilter("REFRESH")
            )

        return view1
    }

    private class RefreshReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            setAdapter()
        }
    }


    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    companion object {
        var recentBinding: FragmentRecentBinding? = null
        var dataAdapter: DataAdapter? = null
        var generatedPDFList = ArrayList<String>()
        var mActivity: Activity? = null

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun setAdapter() {
            val sdf = SimpleDateFormat("MM")
            val cal = Calendar.getInstance()
            val mon = (cal.get(Calendar.MONTH) + 1)
            dataAdapter?.newArray(1)
            val folder: File
            if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cw = ContextWrapper(mActivity?.applicationContext)
                folder = File(
                    cw.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                    mActivity?.resources?.getString(R.string.app_name)
                )
            } else {
                folder = File(BASE_PATH, mActivity?.resources?.getString(R.string.app_name))
            }
            generatedPDFList.clear()
            if (folder.exists()) {
                val files = folder.listFiles()
                for (i in files.indices) {
                    val file = File(files[i].path)
                    if (file.exists()) {
                        if (sdf.format(file.lastModified()).toInt() == mon) {
                            generatedPDFList.add(files[i].path)
                        }
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
                    recentBinding?.mImageRec?.isVisible = true
                    recentBinding?.mNoData?.isVisible = false
                    for (i in generatedPDFList.indices) {
                        dataAdapter?.add(i, generatedPDFList.get(i))
                    }
                } else {
                    recentBinding?.mImageRec?.isVisible = false
                    recentBinding?.mNoData?.isVisible = true
                }
            } else {
                recentBinding?.mImageRec?.isVisible = false
                recentBinding?.mNoData?.isVisible = true
            }
        }
    }
}