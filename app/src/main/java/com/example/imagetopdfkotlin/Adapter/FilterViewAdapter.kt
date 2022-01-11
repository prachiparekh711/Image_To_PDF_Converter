package com.example.imagetopdfkotlin.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alexvasilkov.gestures.views.GestureImageView
import com.bumptech.glide.Glide
import com.example.imagetopdfkotlin.R
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem

class FilterViewAdapter(
    private val mContext: Context,
    thumbnailItemList: List<ThumbnailItem>,
    listener: ThumbnailsAdapterListener
) :
    RecyclerView.Adapter<FilterViewAdapter.MyViewHolder>() {
    private val thumbnailItemList: List<ThumbnailItem>
    private val listener: ThumbnailsAdapterListener
    private var selectedIndex = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_filter_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val thumbnailItem: ThumbnailItem = thumbnailItemList[position]
        Glide.with(mContext)
            .load(thumbnailItem.image)
            .into(holder.thumbnail)

        if (position == 0) {
            holder.mText.text = "Original"
        } else {
            holder.mText.text = thumbnailItem.filterName
        }

        if (selectedIndex == position) {
            holder.thumbnail.background =
                ContextCompat.getDrawable(mContext, R.drawable.image_selected_bg)
        } else {
            holder.thumbnail.background = null
        }

        holder.thumbnail.setOnClickListener(View.OnClickListener {
            listener.onFilterSelected(thumbnailItem.filter)
            selectedIndex = position
            notifyDataSetChanged()
        })
    }

    override fun getItemCount(): Int {
        return thumbnailItemList.size
    }

    interface ThumbnailsAdapterListener {
        fun onFilterSelected(filter: Filter?)
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var thumbnail: GestureImageView
        var mText: TextView

        init {
            thumbnail = view.findViewById(R.id.imgFilterView)
            mText = view.findViewById(R.id.mText)
        }
    }

    init {
        this.thumbnailItemList = thumbnailItemList
        this.listener = listener
    }
}