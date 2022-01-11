package com.example.imagetopdfkotlin.Fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetopdfkotlin.Adapter.FilterViewAdapter
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.BitmapUtils
import com.example.imagetopdfkotlin.Utils.Util
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import java.util.*

class FilterListFragment : Fragment(), FilterViewAdapter.ThumbnailsAdapterListener {
    //    @BindView(R.id.recycler_view)
    var recyclerView: RecyclerView? = null
    var mAdapter: FilterViewAdapter? = null
    var thumbnailItemList: MutableList<ThumbnailItem>? = null
    var listener: FiltersListFragmentListener? = null

    init {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_filter_list, container, false)
        thumbnailItemList = ArrayList<ThumbnailItem>()
        mAdapter = activity?.let {
            FilterViewAdapter(
                it,
                thumbnailItemList as ArrayList<ThumbnailItem>, this
            )
        }
        recyclerView = view.findViewById(R.id.recycler_view)
        val mLayoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = mAdapter
        prepareThumbnail(Util.editedBitmap)
        return view
    }

    fun prepareThumbnail(bitmap: Bitmap?) {
        val r = Runnable {
            val thumbImage: Bitmap?
            thumbImage = if (bitmap == null) {
                activity?.let { BitmapUtils.getBitmapFromAssets(it, IMAGE_NAME, 100, 100) }
            } else {
                Bitmap.createScaledBitmap(bitmap, 100, 100, false)
            }
            if (thumbImage == null) return@Runnable
            ThumbnailsManager.clearThumbs()
            thumbnailItemList!!.clear()

            // add normal bitmap first
            val thumbnailItem = ThumbnailItem()
            thumbnailItem.image = thumbImage
            ThumbnailsManager.addThumb(thumbnailItem)
            val filters: List<Filter> = FilterPack.getFilterPack(requireActivity())
            for (filter in filters) {
                val tI = ThumbnailItem()
                tI.image = thumbImage
                tI.filter = filter
                tI.filterName = filter.name
                ThumbnailsManager.addThumb(tI)
            }
            thumbnailItemList!!.addAll(ThumbnailsManager.processThumbs(requireActivity()))
            requireActivity().runOnUiThread { mAdapter?.notifyDataSetChanged() }
        }
        Thread(r).start()
    }

    override fun onFilterSelected(filter: Filter?) {
        if (listener != null) listener!!.onFilterSelected(filter)
    }

    interface FiltersListFragmentListener {
        fun onFilterSelected(filter: Filter?)
    }

    companion object {
        const val IMAGE_NAME = "/filters/auto_fix.jpg"
    }
}
