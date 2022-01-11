package com.example.imagetopdfkotlin.Adapter

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.imagetopdfkotlin.Activity.InnerFolderActivity.Companion.innerFolderBinding
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import java.io.File
import java.util.*

class BottomImageAdapter(activity: Activity?, from: String?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var activity: Activity? = null
    var arrayList = ArrayList<String>()
    var from: String? = null

    init {
        this.activity = activity
        this.from = from
        arrayList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        var viewHolder: RecyclerView.ViewHolder? = null
        itemView =
            LayoutInflater.from(activity).inflate(R.layout.bottom_image_layout, parent, false)
        viewHolder = MyClassView(itemView)
        return viewHolder
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {
        val options = RequestOptions()
        val file = File(arrayList[position])
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

        if (from.equals("InnerFolderActivity")) {
            holder.mRemove.isVisible = true
            holder.mRemove.setOnClickListener {

                Util.mSelectedImageList.removeAt(position)
                Util.mNewSelectedImageList.removeAt(position)
                arrayList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, arrayList.size)

                innerFolderBinding?.mImportText?.text =
                    "Import (".plus(Util.mNewSelectedImageList.size).plus(")")
                activity?.runOnUiThread {
                    val lbm = activity?.let { it1 -> LocalBroadcastManager.getInstance(it1) }
                    val localIn = Intent("CHANGE_TOP")
                    lbm?.sendBroadcast(localIn)
                }
            }
        } else {
            holder.mRemove.isVisible = false
            holder.mImage.setOnClickListener {
                val lbm = activity?.let { it1 -> LocalBroadcastManager.getInstance(it1) }
                val localIn = Intent("CHANGE_IMAGE")
                localIn.putExtra("pos", position)
                lbm?.sendBroadcast(localIn)
            }
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        Grid
        var mImage: ImageView
        var mRemove: ImageView

        init {
            mImage = itemView.findViewById(R.id.mImage)
            mRemove = itemView.findViewById(R.id.mRemove)

        }
    }

    fun add(i: Int, model: String?) {
        if (model != null) {
            arrayList.add(i, model)
        }
        notifyItemChanged(i)
    }

    fun newArray() {
        arrayList = ArrayList<String>()
        notifyDataSetChanged()
    }

}