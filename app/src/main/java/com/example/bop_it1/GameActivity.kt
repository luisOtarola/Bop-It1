package com.example.bop_it1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.widget.Toast

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log
import android.widget.TextView
import java.util.*



class GameActivity : AppCompatActivity(), SensorEventListener{

    //Sensors
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private val shakeThreshold = 12f
    private val rotationThreshold = 1.0f
    private var lastShakeTime: Long = 0

    //Random instruction
    private lateinit var instructionTextView: TextView
    private val eventInstructions = arrayOf("Bop It", "Swipe It", "Shake It")

    private val handler = Handler()
    private val instructionChangeDelay: Long = 3000 // 3 segundos

    private val updateInstructionRunnable = object : Runnable {
        override fun run() {
            setRandomInstruction()
            handler.postDelayed(this, instructionChangeDelay)
        }
    }

    //Score
    private lateinit var scoreTextView: TextView
    private var score: Int = 0

    //Sound
    private lateinit var backgroundMediaPlayer: MediaPlayer
    private lateinit var fxMediaPlayer: MediaPlayer
    private lateinit var playbackParams: PlaybackParams
    private var volume: Float = 12.0f
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gestureDetector = GestureDetector(this, GestureListener())

        backgroundMediaPlayer = MediaPlayer.create(this, R.raw.background)
        backgroundMediaPlayer.isLooping = true
        backgroundMediaPlayer.start()
        backgroundMediaPlayer.setVolume(volume, volume)

        scoreTextView = findViewById(R.id.scoreTextView)

        playbackParams = backgroundMediaPlayer.playbackParams

        //added
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        instructionTextView = findViewById(R.id.instructionTextView)

        handler.post(updateInstructionRunnable)
    }

    override fun onResume() {
        super.onResume()
        backgroundMediaPlayer.start()
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    override fun onPause() {
        super.onPause()
        backgroundMediaPlayer.pause()
        fxMediaPlayer.stop()
        sensorManager.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerEvent(event)
            Sensor.TYPE_GYROSCOPE -> handleGyroscopeEvent(event)
            // Puedes agregar más casos según sea necesario para otros tipos de sensores
        }
    }

    private fun handleAccelerometerEvent(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        Log.i("acelerometro", "Acelerador: " + acceleration)

        if (acceleration > shakeThreshold && System.currentTimeMillis() - lastShakeTime > 1000) {
            lastShakeTime = System.currentTimeMillis()

            if (instructionTextView.text.toString() == "Shake It") {
                setMusicOnFXMP(R.raw.corret)
                setRandomInstruction()
                updateScore(100)
            }

            showToast("Device Shaken!")
        }
    }

    private fun handleGyroscopeEvent(event: SensorEvent) {
        val xRotation = event.values[0]
        val yRotation = event.values[1]
        val zRotation = event.values[2]

        if (instructionTextView.text.toString() == "Swipe It" &&
            (Math.abs(xRotation) > rotationThreshold || Math.abs(yRotation) > rotationThreshold || Math.abs(zRotation) > rotationThreshold)
        ) {
            setMusicOnFXMP(R.raw.corret)
            showToast("Device Rotated!")
            setRandomInstruction()
            updateScore(100)
        }
    }

    private fun setRandomInstruction() {
        val randomIndex = Random().nextInt(eventInstructions.size)
        val randomInstruction = eventInstructions[randomIndex]
        instructionTextView.text = randomInstruction
    }

    private fun updateScore(points: Int) {
        score += points
        scoreTextView.text = "Score: $score"
    }
    override fun onDestroy() {
        super.onDestroy()
        backgroundMediaPlayer.release()
        fxMediaPlayer.release()
    }

    private fun setMusicOnFXMP(id: Int) {
        fxMediaPlayer = MediaPlayer.create(this, id)
        fxMediaPlayer.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            showToast("onDown")
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            showToast("onSingleTapUp")
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            showToast("onLongPress")
        }

        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent,
            velocityX : Float, velocityY: Float
        ) : Boolean
        {
            showToast("onFling")
            return true
        }

        override fun onScroll(
            e1 : MotionEvent?, e2 : MotionEvent,
            distanceX : Float, distanceY : Float
        ) : Boolean
        {
            showToast("onScroll")
            return true
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
