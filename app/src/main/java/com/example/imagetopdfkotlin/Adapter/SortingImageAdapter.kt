package com.example.imagetopdfkotlin.Adapter

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.imagetopdfkotlin.R

class SortingImageAdapter(
    images: List<String>,
    activity: Activity
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var images: List<String>
    var activity: Activity

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        var itemView: View? = null
        var viewHolder: RecyclerView.ViewHolder? = null
        itemView =
            LayoutInflater.from(activity)
                .inflate(R.layout.sorting_image_layout, parent, false)
        viewHolder = MyClassView(itemView)
        val params = itemView?.layoutParams
        if (params != null) {
            val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width = wm.defaultDisplay.width
            params.height = width / 2
        }

        return viewHolder
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val file = images[position]
        val holder = holder as MyClassView
        holder.mImage.clipToOutline = true

        val options = RequestOptions()
        if (file.endsWith(".PNG") || file.endsWith(".png")) {
            Glide.with(activity)
                .load(file)
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.LOW)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(holder.mImage)
        } else {
            Glide.with(activity)
                .load(file)
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.LOW)
                )
                .into(holder.mImage)
        }

        holder.mIndex.text = (position + 1).toString()
    }

    override fun getItemCount(): Int {
        return images.size
    }


    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImage: ImageView
        var mIndex: TextView

        init {
            mImage = itemView.findViewById(R.id.mImage)
            mIndex = itemView.findViewById(R.id.mIndex)
        }
    }

    init {
        this.images = images
        this.activity = activity
    }
}
