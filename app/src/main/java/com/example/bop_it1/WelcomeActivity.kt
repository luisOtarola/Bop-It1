package com.example.bop_it1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity: AppCompatActivity()
{
    val tagLog = "SplashActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


        // Getting preference
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        // Read a preference
        val value = sharedPreferences.getString("splash_time", "3000")

        Log.i(tagLog,"Value of splash_time:" + value)

        // Load the fade-in animation
        val fadeIn = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)

        // Load the growth animation
        val scaleUp = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)

        // Create an AnimationSet to combine both animations
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(fadeIn)
        animationSet.addAnimation(scaleUp)


        val imageView = findViewById<ImageView>(R.id.imageSplashLayout)

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Animation started
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Animation ended; proceed to MainActivity
                val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                startActivity(intent)
                finish() // Optional: Close the splash screen activity
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // Animation repeated
            }
        })
        imageView.startAnimation(fadeIn)
        val seconds = value?.toLong()
        val delayMillis = seconds!!.toLong() // 2 seconds (adjust as needed)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: Close the splash screen activity
        }, delayMillis)
    }
}




