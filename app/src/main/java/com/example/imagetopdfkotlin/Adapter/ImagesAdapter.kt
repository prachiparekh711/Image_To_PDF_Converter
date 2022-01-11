package com.example.imagetopdfkotlin.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.imagetopdfkotlin.Activity.InnerFolderActivity
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.Utils.Util.Companion.itemRemoved
import java.io.File
import java.util.*

class ImagesAdapter(activity: Activity?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var activity: Activity? = null
    var mArrayList = ArrayList<String>()


    init {
        this.activity = activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        var viewHolder: RecyclerView.ViewHolder? = null
        itemView = LayoutInflater.from(activity).inflate(R.layout.image_grid_view, parent, false)
        viewHolder = MyClassView(itemView)
        val params = itemView.layoutParams
        if (params != null) {
            val wm = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width = wm.defaultDisplay.width
            params.height = width / 2
        }
        return viewHolder
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {
        val options = RequestOptions()
        val file = File(mArrayList[position])
        val holder = holders as MyClassView
        if (file.exists()) {
            holder.mImage.clipToOutline = true
            Glide.with(activity!!)
                .load(file.path)
                .apply(
                    options.centerCrop()
                        .skipMemoryCache(true)
                        .priority(Priority.LOW)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(holder.mImage)
        }



        holder.mImage.setOnClickListener {

            if (file.path in Util.mSelectedImageList) {
                holder.mExpand.isVisible = true
                holder.mCount.isGone = true
                holder.mSelectedImg.isGone = true
                itemRemoved = true
                Util.mSelectedImageList.remove(file.path)
                Util.mNewSelectedImageList.remove(file.path)
                notifyDataSetChanged()
            } else {
                Util.mSelectedImageList.add(file.path)
                Util.mNewSelectedImageList.add(file.path)
                holder.mExpand.isGone = true
                holder.mCount.isVisible = true
                holder.mSelectedImg.isVisible = true
                holder.mCount.text = (Util.mSelectedImageList.indexOf(file.path) + 1).toString()
            }

            InnerFolderActivity.innerFolderBinding?.mImportText?.text =
                "Import (".plus(Util.mNewSelectedImageList.size).plus(")")
            activity?.runOnUiThread {
                val lbm = activity?.let { it1 -> LocalBroadcastManager.getInstance(it1) }
                val localIn = Intent("CHANGE_BOTTOM")
                lbm?.sendBroadcast(localIn)
            }
        }

        if (itemRemoved) {
            if (Util.mSelectedImageList.contains(file.path)) {
                holder.mCount.text = (Util.mSelectedImageList.indexOf(file.path) + 1).toString()
            }
        }

        Log.e("Util size", Util.mSelectedImageList.size.toString())
        if (Util.mSelectedImageList.size > 0) {
            for (i in 0 until Util.mSelectedImageList.size) {
                if (Util.mSelectedImageList[i].equals(file.path)) {
                    holder.mExpand.isGone = true
                    holder.mCount.isVisible = true
                    holder.mSelectedImg.isVisible = true
                    holder.mCount.text = (Util.mSelectedImageList.indexOf(file.path) + 1).toString()
                    break
                } else {
                    holder.mExpand.isVisible = true
                    holder.mCount.isGone = true
                    holder.mSelectedImg.isGone = true
                }
            }
        } else {
            holder.mExpand.isVisible = true
            holder.mCount.isGone = true
            holder.mSelectedImg.isGone = true
        }

        holder.mExpand.setOnClickListener {
            val lbm = activity?.let { it1 -> LocalBroadcastManager.getInstance(it1) }
            val localIn = Intent("OPEN_IMAGE")
            localIn.putExtra("pos", position)
            lbm?.sendBroadcast(localIn)
        }
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImage: ImageView
        var mSelectedImg: ImageView
        var mExpand: ImageView
        var mCount: TextView

        init {
            mImage = itemView.findViewById(R.id.mImage)
            mSelectedImg = itemView.findViewById(R.id.mSelectedImg)
            mExpand = itemView.findViewById(R.id.mExpand)
            mCount = itemView.findViewById(R.id.mCount)
        }
    }

    fun add(i: Int, model: String?) {
        if (model != null) {
            mArrayList.add(i, model)
        }
        notifyItemChanged(i)
    }

}