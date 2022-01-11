package com.example.imagetopdfkotlin.Adapter

import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.imagetopdfkotlin.Activity.InnerFolderActivity
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import java.io.File
import java.util.*

class ShowImageAdapter(var activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mArrayList = ArrayList<String>()


    init {
        this.activity = activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        var viewHolder: RecyclerView.ViewHolder? = null
        itemView =
            LayoutInflater.from(activity).inflate(R.layout.expand_image_layout, parent, false)
        viewHolder = MyClassView(itemView)

        return viewHolder
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {
        val options = RequestOptions()
        val file = File(mArrayList[position])
        val holder = holders as MyClassView
        if (file.exists()) {
            holder.mImage.clipToOutline = true
            Glide.with(activity)
                .load(file.path)
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.LOW)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(holder.mImage)
        }
        InnerFolderActivity.innerFolderBinding?.mCheckbox?.isSelected =
            Util.mSelectedImageList.contains(file.path)
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImage: ImageView


        init {
            mImage = itemView.findViewById(R.id.mImage)

        }
    }

    fun add(i: Int, model: String?) {
        if (model != null) {
            mArrayList.add(i, model)
        }
        notifyItemChanged(i)
    }

}