package com.example.imagetopdfkotlin.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.example.imagetopdfkotlin.Activity.MainActivity
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import java.io.File
import java.util.*

class SelectedImageAdapter(activity: Activity?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var activity: Activity? = null
    var arrayList = ArrayList<String>()
    var type: Int = 0

    init {
        this.activity = activity
        arrayList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var itemView: View? = null
        var viewHolder: RecyclerView.ViewHolder? = null

        when (type) {
            0 -> {
                itemView =
                    LayoutInflater.from(activity)
                        .inflate(R.layout.selected_img_layout, parent, false)
                viewHolder = MyClassView(itemView)
            }
            1 -> {
                itemView =
                    LayoutInflater.from(activity).inflate(R.layout.selection_layout, parent, false)
                viewHolder = MyClassView1(itemView)
            }
        }

        val params = itemView?.layoutParams
        if (params != null) {
            val wm = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width = wm.defaultDisplay.width
            params.height = width / 2
        }

        return viewHolder!!
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {
        val options = RequestOptions()

        when (type) {
            0 -> {
                val holder = holders as MyClassView
                if (position == arrayList.size - 1) {
                    holder.mAdd.isVisible = true
                } else {
                    val file = File(arrayList[position])
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
                    holder.mAdd.isVisible = false
                }

                holder.mAdd.setOnClickListener {
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.putExtra("from", "add_more")
                    activity?.startActivity(intent)
                }
            }
            1 -> {
                val holder = holders as MyClassView1
                val file = File(arrayList[position])
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

                    holder.mCheckBox.isSelected = Util.mEditedImageList.contains(file.path)

                    holder.mImage.setOnClickListener {
                        if (file.path in Util.mNewSelectedImageList) {
                            Util.mEditedImageList.remove(file.path)
                            Util.mNewSelectedImageList.remove(file.path)
                        } else {
                            Util.mEditedImageList.add(file.path)
                            Util.mNewSelectedImageList.add(file.path)
                        }
                        notifyDataSetChanged()
                    }
                }
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return type
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImage: ImageView
        var mAdd: ImageView

        init {
            mImage = itemView.findViewById(R.id.mImage)
            mAdd = itemView.findViewById(R.id.mAdd)
        }
    }

    class MyClassView1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImage: ImageView
        var mCheckBox: ImageView

        init {
            mImage = itemView.findViewById(R.id.mImage)
            mCheckBox = itemView.findViewById(R.id.mCheckbox)
        }
    }

    fun add(i: Int, model: String?) {
        if (model != null) {
            arrayList.add(i, model)
        }
        notifyItemChanged(i)
    }

    fun newArray(type: Int) {
        arrayList = ArrayList<String>()
        this.type = type
        notifyDataSetChanged()
    }

}