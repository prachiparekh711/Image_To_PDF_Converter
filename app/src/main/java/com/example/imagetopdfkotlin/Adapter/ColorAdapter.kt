package com.example.imagetopdfkotlin.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util.Companion.mDoodleColor
import java.util.*

class ColorAdapter(
    listner: ColorClickListener,
    colorList: ArrayList<Int>?,
    context: Context
) :
    RecyclerView.Adapter<ColorAdapter.ViewHolder>() {
    var context: Context
    var mClickListener: ColorClickListener
    var viewHolder: ViewHolder? = null
    var itemLayoutView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        itemLayoutView = LayoutInflater.from(parent.context)
            .inflate(R.layout.color_item, parent, false)
        viewHolder = ViewHolder(itemLayoutView)
        return viewHolder!!
    }

    override fun onBindViewHolder(viewHolder1: ViewHolder, position: Int) {
        try {
            val color = colorList?.get(position)?.let { context.resources.getColor(it) }!!
            viewHolder1.imgViewIcon.setBackgroundColor(color)
            if (mDoodleColor.equals(color)) {
                viewHolder1.borderCard.isVisible = true
                viewHolder1.borderCard.setCardBackgroundColor(color)
            } else {
                viewHolder1.borderCard.isVisible = false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewHolder1.imgViewIcon.setOnClickListener {
            mClickListener.onColorClick(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return colorList!!.size
    }

    interface ColorClickListener {
        fun onColorClick(position: Int)
    }

    class ViewHolder(itemLayoutView: View?) : RecyclerView.ViewHolder(
        itemLayoutView!!
    ) {
        var imgViewIcon: ImageView
        var borderCard: CardView

        init {
            imgViewIcon = itemLayoutView!!.findViewById(R.id.item_image)
            borderCard = itemLayoutView.findViewById(R.id.mBorderCard)
        }
    }

    companion object {
        var colorList: ArrayList<Int>? = null
    }

    init {
        Companion.colorList = colorList
        this.context = context
        mClickListener = listner
    }
}

