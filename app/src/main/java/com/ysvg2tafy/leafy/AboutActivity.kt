package com.ysvg2tafy.leafy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)

        supportActionBar!!.title=""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()

        }


        val version=BuildConfig.VERSION_NAME
        about_text.text=version+"\n\n"+resources.getString(R.string.large_text)


    }
}
