package com.ysvg2tafy.leafy

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.metalab.asyncawait.async
import kotlinx.android.synthetic.main.activity_share.*
import java.util.*


class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        setSupportActionBar(toolbar)
        supportActionBar!!.title="Leafy"

        progressBar2.visibility= View.VISIBLE

        primary_disease.visibility= View.INVISIBLE
        disease_name.visibility= View.INVISIBLE
        learn_more.visibility= View.INVISIBLE



        async {

            val intent = intent
            val type = intent.type
            val action = intent.action
            var imageUri: Uri?=null
            var bitmap:Bitmap?=null
            if (Intent.ACTION_SEND == action && type != null) {
                if (type.startsWith("image/")) {
                    imageUri=intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
                    display.setImageURI(imageUri)
                    bitmap = MediaStore.Images.Media.getBitmap(this@ShareActivity.getContentResolver(), imageUri)

                }
            }

            val ans=await {
                Classifier(this@ShareActivity, bitmap!!,"leaf_label.txt","leaf-cnn.tflite")
            }
            /*for((key,value) in ans){
                if (value>0.0){
                    println(key +value.toString())
                }
            }*/
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
                    val intent=Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q=" + disease_name.text.toString()))
                    startActivity(intent)
                }catch (ex: Exception){
                    Toast.makeText(this@ShareActivity, ex.toString(), Toast.LENGTH_SHORT).show()
                }

            })


        }

    }



    override fun onDestroy() {
        super.onDestroy()
        async.cancelAll()
        this.cacheDir.deleteRecursively()
    }

}
