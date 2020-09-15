package com.ysvg2tafy.leafy

import android.app.SearchManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.metalab.asyncawait.async
import kotlinx.android.synthetic.main.activity_classify.*
import kotlinx.android.synthetic.main.activity_classify.disease_name
import kotlinx.android.synthetic.main.activity_classify.display
import kotlinx.android.synthetic.main.activity_classify.learn_more
import kotlinx.android.synthetic.main.activity_classify.primary_disease
import kotlinx.android.synthetic.main.activity_classify.progressBar2
import kotlinx.android.synthetic.main.activity_classify.toolbar
import kotlinx.android.synthetic.main.activity_share.*
import java.util.*


class ClassifyActivity : AppCompatActivity() {

    lateinit var result:Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classify)

        setSupportActionBar(toolbar)
        supportActionBar!!.title=""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        progressBar2.visibility=View.VISIBLE

        primary_disease.visibility=View.INVISIBLE
        disease_name.visibility=View.INVISIBLE
        learn_more.visibility=View.INVISIBLE



        async {

            result = await {
                rotate_image(TARGET_PATH!!)!!

            }

            display.setImageBitmap(result)

            val ans=await {
                Classifier(this@ClassifyActivity, result,"leaf_label.txt","leaf-cnn.tflite")
            }
            /*var s=""
            for((key,value) in ans){
                if (true){
                    //println(key +value.toString())
                    s=s+key+"-"+value.toString()+"\n"
                }
            }
            println(s)*/

            /*val fWriter = FileWriter(this@ClassifyActivity.externalCacheDir.toString()+"Output.txt")
            for((key,value) in ans){
                fWriter.write(key +" - "+value.toString()+"\n")
                fWriter.flush()
            }
            fWriter.close()*/

            val maxValueInMap: Float = Collections.max(ans.values)
            for ((key, value) in ans) {
                if (value == maxValueInMap) {
                    disease_name.text=key
                }
            }

            progressBar2.visibility=View.INVISIBLE

            primary_disease.visibility=View.VISIBLE
            disease_name.visibility=View.VISIBLE
            learn_more.visibility=View.VISIBLE


            learn_more.setOnClickListener(View.OnClickListener { //https://www.google.com/#q=
                //Toast.makeText(this@MainActivity, textView.getText(), Toast.LENGTH_SHORT).show()
                try {
                    /*val intent=Intent(Intent.ACTION_WEB_SEARCH)
                    intent.putExtra(SearchManager.QUERY,disease_name.text.toString())

                    startActivity(intent)*/
                    val intent=Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + disease_name.text.toString()))
                    startActivity(intent)
                }catch (ex: Exception){
                    Toast.makeText(this@ClassifyActivity, ex.toString(), Toast.LENGTH_SHORT).show()
                }

            })


        }


    }


    fun rotate_image(path:String): Bitmap? {
        val ei = ExifInterface(path)
        var bitmap= BitmapFactory.decodeFile(path)
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
        return rotatedBitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        async.cancelAll()
        this.cacheDir.deleteRecursively()
    }

}
