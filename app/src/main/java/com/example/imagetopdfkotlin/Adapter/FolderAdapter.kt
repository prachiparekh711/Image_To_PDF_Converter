package com.example.imagetopdfkotlin.Adapter

import android.app.Activity
import android.content.Context
import android.net.Uri
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
import com.example.imagetopdfkotlin.Interface.FolderInterface
import com.example.imagetopdfkotlin.Model.BaseModel
import com.example.imagetopdfkotlin.R
import java.io.File
import java.util.*

class FolderAdapter(activity: Activity?, folderInterface: FolderInterface?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var activity: Activity? = null
    var arrayList = ArrayList<BaseModel>()
    var folderInterface: FolderInterface? = null

    init {
        this.activity = activity
        this.folderInterface = folderInterface
        arrayList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View
        var viewHolder: RecyclerView.ViewHolder? = null
        itemView = LayoutInflater.from(activity).inflate(R.layout.folder_grid_view, parent, false)
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
        val holder = holders as MyClassView
        val item = arrayList[position]
        holder.mImage.clipToOutline = true
        if (item.pathlist?.size!! > 0) {
            val uri = Uri.fromFile(File(item.pathlist!!.get(0)))
            try {
                Glide.with(activity!!)
                    .load(uri)
                    .apply(
                        options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(holder.mImage)
            } catch (e: Exception) {
                Glide.with(activity!!)
                    .load(uri)
                    .apply(
                        options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW)
                    )
                    .into(holder.mImage)
            }
        }
        holder.mAlbumName!!.text = item.bucketName
        if (item.pathlist!!.size > 1)
            holder.mCount!!.text = item.pathlist!!.size.toString().plus(" Photos")
        else
            holder.mCount!!.text = item.pathlist!!.size.toString().plus(" Photo")
        holder.mImage.setOnClickListener { folderInterface!!.OnSelectFolder(item) }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        Grid
        var mImage: ImageView

        var mAlbumName: TextView? = null
        var mCount: TextView? = null

        init {
            mImage = itemView.findViewById(R.id.mImage)

            mAlbumName = itemView.findViewById(R.id.mAlbumName)
            mCount = itemView.findViewById(R.id.mCount)
        }
    }

    fun add(i: Int, model: BaseModel) {
        arrayList.add(i, model)
        notifyItemChanged(i)
    }

}