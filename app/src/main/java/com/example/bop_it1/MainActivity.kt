package com.example.bop_it1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    //private var globalCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        // Local variable
        val localCounter = 0
        // Find views
        //val buttonIncrement = findViewById<Button>(R.id.buttonIncrement)
        //val buttonNavigate = findViewById<Button>(R.id.buttonNavigate)
        // Button click listeners
        buttonIncrement.setOnClickListener {
        // Increment the global counter
            globalCounter++
        // Update the text on a TextView (not shown in this example)
        }
        buttonNavigate.setOnClickListener {
        // Condition (if-else)
            if (globalCounter >= 5) {
        // If the globalCounter is 5 or more, navigate to another
                activity
                val intent = Intent(this, AnotherActivity::class.java)
                startActivity(intent)
            } else {
        // Else, display a message
        // This is where you might show a toast or update a TextView
            }
        }
    }

    // Switch statement (for demonstration)
    var number = 2
    when (number)
    {
        1 -> println("Number is 1")
        2 -> println("Number is 2")
        else -> println("Number is neither 1 nor 2")
    }*/
    }
}