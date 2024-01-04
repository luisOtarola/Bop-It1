package com.example.bop_it1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var aboutButton: Button
    private lateinit var preferencesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aboutButton = findViewById(R.id.btn_about) as Button
        preferencesButton = findViewById(R.id.btn_preferences) as Button

        aboutButton.setOnClickListener {
            val aboutIntent = Intent(this, AboutActivity::class.java)
            startActivity(aboutIntent)
        }

        preferencesButton.setOnClickListener {
            val preferencesIntent = Intent(this, PreferenceActivity::class.java)
            startActivity(preferencesIntent)
        }
    }
}
