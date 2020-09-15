package com.ysvg2tafy.leafy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import pl.aprilapps.easyphotopicker.*

class MainActivity : AppCompatActivity() {

    lateinit var easyImage:EasyImage
    private val CHOOSER_PERMISSIONS_REQUEST_CODE = 7459

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        easyImage = EasyImage.Builder(this) // Chooser only
            // Will appear as a system chooser title, DEFAULT empty string
            .setChooserTitle("Pick media")
            // Will tell chooser that it should show documents or gallery apps
            //.setChooserType(ChooserType.CAMERA_AND_DOCUMENTS)  you can use this or the one below
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            // Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
            .setCopyImagesToPublicGalleryFolder(false) // Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
            .setFolderName("EasyImage sample") // Allow multiple picking
            .allowMultiple(false)
            .build()

        media_btn.setOnClickListener {
            val necessaryPermissions = arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openChooser(this@MainActivity)
            } else {
                requestPermissionsCompat(necessaryPermissions, CHOOSER_PERMISSIONS_REQUEST_CODE)
            }

        }

        about_btn.setOnClickListener {
            startActivity(Intent(this,AboutActivity::class.java))
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openChooser(this@MainActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                //println(imageFiles)
                //Log.i("RDX",imageFiles.toString())
                //Log.i("RDX2",photos.toString())




                val intent=Intent(this@MainActivity,ClassifyActivity::class.java)
                TARGET_PATH=imageFiles[0].file.path
                startActivity(intent)
                //Toast.makeText(this@MainActivity,"done",Toast.LENGTH_SHORT).show()

                /*val ei = ExifInterface(imageFiles[0].file.path)
                val orientation: Int = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED)

                fun rotateImage(source: Bitmap, angle:Float): Bitmap? {
                    var matrix = Matrix()
                    matrix.postRotate(angle);
                    return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                        matrix, true);
                }

                var rotatedBitmap: Bitmap? = null
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(bitmap, 270f)
                    ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
                    else -> rotatedBitmap = bitmap
                }


                display.setImageBitmap(rotatedBitmap)*/
            }

            override fun onImagePickerError(error: Throwable, source: MediaSource) {
                //Some error handling
                //error.printStackTrace()
            }

            override fun onCanceled(source: MediaSource) {
                //Not necessary to remove any files manually anymore
            }
        })

    }

    private fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    private fun requestPermissionsCompat(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this@MainActivity, permissions, requestCode)
    }



    override fun onBackPressed() {
        this.cacheDir.deleteRecursively()
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
