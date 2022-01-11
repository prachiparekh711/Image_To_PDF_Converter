package com.example.imagetopdfkotlin.Activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.imagetopdfkotlin.R
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

abstract class BaseActivity : AppCompatActivity(), PermissionCallbacks {
    var perms1 = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    abstract fun permissionGranted()

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        readExternalStorage()
        ActivityCompat.requestPermissions(
            this,
            perms1, 1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * Read external storage file
     */
    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private fun readExternalStorage() {
        val isGranted = EasyPermissions.hasPermissions(this, *perms1)
        if (isGranted) {
            permissionGranted()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.vw_rationale_storage),
                RC_READ_EXTERNAL_STORAGE, *perms1
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
//        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        permissionGranted()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
//        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // If Permission permanently denied, ask user again
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.vw_rationale_storage),
                RC_READ_EXTERNAL_STORAGE, *perms1
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (EasyPermissions.hasPermissions(this, *perms1)) {
                permissionGranted()
            } else {
                EasyPermissions.requestPermissions(
                    this, getString(R.string.vw_rationale_storage),
                    RC_READ_EXTERNAL_STORAGE, *perms1
                )
            }
        } else if (requestCode == RC_READ_EXTERNAL_STORAGE) {
            val isGranted = EasyPermissions.hasPermissions(this, *perms1)
            if (!isGranted) {
                EasyPermissions.requestPermissions(
                    this, getString(R.string.vw_rationale_storage),
                    RC_READ_EXTERNAL_STORAGE, *perms1
                )
            }
        }
    }

    companion object {
        private const val RC_READ_EXTERNAL_STORAGE = 123
        val TAG = BaseActivity::class.java.name
    }
}