package com.example.imagetopdfkotlin.Adapter

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetopdfkotlin.R
import java.io.File
import java.util.*

class DataAdapter(activity: Activity?) :
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
        itemView =
            LayoutInflater.from(activity).inflate(R.layout.data_layout, parent, false)

        viewHolder = MyClassView(itemView)

        return viewHolder
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holders: RecyclerView.ViewHolder, position: Int) {
        val holder = holders as MyClassView
        val file = File(arrayList.get(position))

        holder.pdfName.text = file.name
        holder.pdfPath.text = file.path

        holder.mMore.setOnClickListener {
            val lbm = activity?.let { it1 -> LocalBroadcastManager.getInstance(it1) }
            var localIn: Intent? = null
            when (type) {
                0, 1 -> {
                    localIn = Intent("MORE")
                }
                2 -> {
                    localIn = Intent("MORE_ALL")
                }
            }

            localIn?.putExtra("file", file.path)
            if (type == 0)
                localIn?.putExtra("from", "BOOKMARK")
            if (localIn != null) {
                lbm?.sendBroadcast(localIn)
            }
        }

        holder.mShare.setOnClickListener {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            if (file.exists() == true) {
                val URI = activity?.let { it1 ->
                    FileProvider.getUriForFile(
                        it1,
                        activity?.packageName + ".provider",
                        file
                    )
                }
                intentShareFile.type = "application/pdf"
                intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intentShareFile.putExtra(Intent.EXTRA_STREAM, URI)
                intentShareFile.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Sharing File..."
                )
                intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
                activity?.startActivity(Intent.createChooser(intentShareFile, "Share File"))
            }
        }

        holder.mMainLL.setOnClickListener {
            val viewPdf = Intent(Intent.ACTION_VIEW)
            viewPdf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            val URI = file.let {
                activity?.let { it1 ->
                    FileProvider.getUriForFile(
                        it1,
                        activity?.packageName + ".provider",
                        it
                    )
                }
            }
            viewPdf.setDataAndType(URI, "application/pdf")
            viewPdf.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            activity?.startActivity(viewPdf)
        }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }


    class MyClassView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pdfName: TextView
        var pdfPath: TextView
        var mShare: ImageView
        var mMore: ImageView
        var mMainLL: LinearLayout

        init {
            pdfName = itemView.findViewById(R.id.pdfName)
            pdfPath = itemView.findViewById(R.id.pdfPath)
            mShare = itemView.findViewById(R.id.mShare)
            mMore = itemView.findViewById(R.id.mMore)
            mMainLL = itemView.findViewById(R.id.mainLL)
        }
    }


    fun add(i: Int, model: String?) {
        if (model != null) {
            arrayList.add(i, model)
        }
        notifyItemChanged(i)
    }

    fun newArray(type: Int) {
        //0 BOOKMARK
        //1 RECENT
        //2 ALL
        arrayList = ArrayList<String>()
        this.type = type
        notifyDataSetChanged()
    }
}

