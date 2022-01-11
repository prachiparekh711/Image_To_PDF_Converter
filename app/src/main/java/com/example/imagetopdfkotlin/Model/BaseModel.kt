package com.example.imagetopdfkotlin.Model

import java.io.Serializable
import java.util.*

class BaseModel : Serializable {
    var isDirectory = false
    var id: String? = null
    var did: String? = null
    var name: String? = null
    var folderName: String? = null
    var path: String? = null
    var size //byte
            : Long = 0
    var bucketId //Directory ID
            : String? = null
    var bucketName: String? = null
    var bucketPath //Directory Name
            : String? = null
    var date: String? = null
    var pathlist: ArrayList<String>? = null
    var type = 0
}
