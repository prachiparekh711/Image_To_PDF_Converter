package com.example.imagetopdfkotlin.Fragment

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.imagetopdfkotlin.Activity.AllPDFActivity
import com.example.imagetopdfkotlin.Activity.GalleryActivity
import com.example.imagetopdfkotlin.Activity.SelectedImageActivity
import com.example.imagetopdfkotlin.Model.BaseModel
import com.example.imagetopdfkotlin.R
import com.example.imagetopdfkotlin.Utils.Util
import com.example.imagetopdfkotlin.databinding.FragmentHomeBinding
import com.isseiaoki.simplecropview.util.Utils.*
import es.dmoral.toasty.Toasty
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    var fragmentMainBinding: FragmentHomeBinding? = null
    var imageUri: Uri? = null
    var imageUri1: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        fragmentMainBinding?.camera?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
//                val values = ContentValues()
//                values.put(MediaStore.Images.Media.TITLE, "New Picture")
//                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
//                imageUri = activity!!.contentResolver.insert(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
//                )
                Log.e("Camera", "openable")
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, 101)
            }
        })

        fragmentMainBinding?.gallery?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(requireActivity(), GalleryActivity::class.java)
                requireActivity().startActivity(intent)
            }
        })

        fragmentMainBinding?.fileManager?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageUri1)
                startActivityForResult(intent, 102)
            }
        })

        fragmentMainBinding?.generatedPdf?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(requireActivity(), AllPDFActivity::class.java)
                requireActivity().startActivity(intent)
            }
        })

        return fragmentMainBinding?.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    var values: ContentValues? = null

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("result", resultCode.toString())
        Log.e("request", requestCode.toString())

        if (requestCode == 101 && resultCode == RESULT_OK) {
            val photo = data!!.extras!!["data"] as Bitmap?
            imageUri = getImageUri(requireContext(), photo)
            if (imageUri != null) {
                try {
                    val imageurl: String? = Util.getRealPathFromURI(requireContext(), imageUri)
                    if (imageurl != null) {
                        Log.e("LLL_Path: ", imageurl)
                    }
                    val img = BaseModel()
                    val file = File(imageurl)
                    img.id = 1.toString()
                    img.name = file.name
                    img.path = file.absolutePath
                    img.size = file.length()
                    img.bucketName = file.parentFile.name
                    img.date = Util.convertTimeDateModified(file.lastModified())
                    Util.mNewSelectedImageList.add(file.absolutePath)
                    Util.mSelectedImageList.add(file.absolutePath)
                    val intent = Intent(requireContext(), SelectedImageActivity::class.java)
                    intent.putExtra("isClosed", true)
                    startActivity(intent)
                    System.gc()
                    values?.clear()
                    values = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toasty.error(requireContext(), "Null", Toasty.LENGTH_LONG).show()
            }
        } else if (requestCode == 101 && resultCode == RESULT_CANCELED) {
            values?.clear()
            values = null
        } else if (requestCode == 102 && resultCode == RESULT_OK) {
            if (data != null && data.data != null) {
                val uri: Uri = data.data!!
                val selectedFile = getPathFromUri(requireContext(), uri)
                if (selectedFile != null) {
                    Util.mNewSelectedImageList.add(selectedFile)
                    Util.mSelectedImageList.add(selectedFile)
                    System.gc()
                    val intent = Intent(requireActivity(), SelectedImageActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null && data.data != null) {
                val photo = data.extras!!["data"] as Bitmap?
                val uri: Uri = getImageUri(requireActivity().applicationContext, photo)
                val selectedFile = getPathFromUri(requireContext(), uri)
                if (selectedFile != null) {
                    Util.mNewSelectedImageList.add(selectedFile)
                    Util.mSelectedImageList.add(selectedFile)
                    System.gc()
                    val intent = Intent(requireActivity(), SelectedImageActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap?): Uri {
        val OutImage = Bitmap.createScaledBitmap(inImage!!, 1000, 1000, true)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, OutImage, "Title", null)
        return Uri.parse(path)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getPathFromUri(context: Context?, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return ""
    }
}