package com.example.bop_it1

import android.annotation.SuppressLint
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
import androidx.preference.PreferenceManager
import java.util.*


class GameActivity : AppCompatActivity(), SensorEventListener {

    //Sensors
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private val gravedadshakeThreshold = 10.0f
    private var lastShakeTime: Long = 0

    //Shared from setting
    private var shakeDetectionMethod: String = "Acelerometro"
    private var shakeThreshold = 2.0f + gravedadshakeThreshold
    private var rotationThreshold = 1.0f
    /*var shakeThreshold: Float = 2.0f + gravedadshakeThreshold
        get() = field
        set(value) {
            field = value
            // Aquí puedes realizar acciones adicionales si es necesario
        }

    var rotationThreshold: Float = 1.0f
        get() = field
        set(value) {
            field = value
            // Aquí puedes realizar acciones adicionales si es necesario
        }*/

    //Random instruction
    private lateinit var instructionTextView: TextView
    private val eventInstructions = arrayOf("Bop It", "Gira", "Desliza")

    //Handler and time for action
    private val handler = Handler()
    private var instructionChangeDelay: Long = 3000 // 3 segundos
    private var timeRemaining: Long = instructionChangeDelay

    private val updateInstructionRunnable = object : Runnable {
        override fun run() {
            setRandomInstruction()
            handler.postDelayed(this, timeRemaining)
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

    // Nuevas variables para controlar el estado del juego
    private var isFirstActionCompleted = false
    private var hasLost = false
    private var isCurrentActionCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gestureDetector = GestureDetector(this, GestureListener())

        backgroundMediaPlayer = MediaPlayer.create(this, R.raw.background)
        backgroundMediaPlayer.isLooping = true
        backgroundMediaPlayer.start()
        //backgroundMediaPlayer.setVolume(volume, volume)

        scoreTextView = findViewById(R.id.scoreTextView)
        playbackParams = backgroundMediaPlayer.playbackParams

        //confifuracion sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        instructionTextView = findViewById(R.id.instructionTextView)

        handler.post(updateInstructionRunnable)

        //SharedPreferences conectada con settings
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        //shakeThreshold = sharedPreferences.getFloat("shake_threshold", 2.0f)
        //rotationThreshold = sharedPreferences.getFloat("rotation_threshold", 1.0f)
        shakeDetectionMethod = sharedPreferences.getString("shake_detection_method", "Acelerometro") ?: "Acelerometro"

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

            if (instructionTextView.text.toString() == "Desliza" && shakeDetectionMethod == "Acelerometro") {
                setMusicOnFXMP(R.raw.corret)
                setRandomInstruction()
                updateScore(100)
                restartTimer()
            } else if (instructionTextView.text.toString() != "Desliza" && shakeDetectionMethod == "Acelerometro") {
                Log.i("Perdi", "Perdi con acelerometro")
            }

            showToast("Device Shaken!")
        }
    }

    private fun handleGyroscopeEvent(event: SensorEvent) {
        val xRotation = event.values[0]
        val yRotation = event.values[1]
        val zRotation = event.values[2]

        if (instructionTextView.text.toString() == "Gira" &&
            (Math.abs(xRotation) > rotationThreshold || Math.abs(yRotation) > rotationThreshold || Math.abs(
                zRotation
            ) > rotationThreshold)
        ) {
            setMusicOnFXMP(R.raw.corret)
            showToast("Device Rotated!")
            setRandomInstruction()
            updateScore(100)
            restartTimer()
        } else if (instructionTextView.text.toString() != "Gira" &&
            (Math.abs(xRotation) > rotationThreshold || Math.abs(yRotation) > rotationThreshold || Math.abs(
                zRotation
            ) > rotationThreshold)
        ) {
            Log.i("Perdi", "Perdi con giroscopio")
            Log.i("Perdi x", "Perdi con giroscopio x: " + Math.abs(xRotation))
            Log.i("Perdi y", "Perdi con giroscopio y: " + Math.abs(yRotation))
            Log.i("Perdi z", "Perdi con giroscopio z: " + Math.abs(zRotation))
            Log.i("Perdi var", "Perdi con giroscopio var: " + rotationThreshold)
        }
    }

    private fun restartTimer() {
        timeRemaining = instructionChangeDelay
        handler.removeCallbacks(updateInstructionRunnable)
        handler.postDelayed(updateInstructionRunnable, timeRemaining)
        isCurrentActionCompleted = false
    }

    private fun setRandomInstruction() {
        val randomIndex = Random().nextInt(eventInstructions.size)
        val randomInstruction = eventInstructions[randomIndex]
        instructionTextView.text = randomInstruction
    }

    @SuppressLint("SetTextI18n")
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

        override fun onSingleTapUp(e: MotionEvent): Boolean {

            if (instructionTextView.text.toString() == "Bop It") {

                // Verificación adicional para "Bop It"
                setMusicOnFXMP(R.raw.corret)
                setRandomInstruction()
                updateScore(100) // Aumentar el puntaje en 100 puntos
                restartTimer()
            } else if (instructionTextView.text.toString() != "Bop It") {
                Log.i("Perdi", "Perdi con un toque")
            }
            showToast("onSingleTapUp")
            return true
        }

        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent,
            distanceX: Float, distanceY: Float
        ): Boolean {

            /*if (!isFirstActionCompleted) {
                isFirstActionCompleted = true
            } else {
                handleLoss()
                return true
            }
            if (!isCurrentActionCompleted) {
                isCurrentActionCompleted = true
            }*/
            Log.i("DesliceGiroscopio", "DesliceGiroscopio: " + shakeDetectionMethod)

            if (instructionTextView.text.toString() == "Desliza" && shakeDetectionMethod == "Deslizar") {

                Log.i("DesliceGiroscopio", "DesliceGiroscopio: " + shakeDetectionMethod)
                // Verificación adicional para "Bop It"
                setMusicOnFXMP(R.raw.corret)
                setRandomInstruction()
                updateScore(100) // Aumentar el puntaje en 100 puntos
                restartTimer()
            } else if (instructionTextView.text.toString() != "Desliza" && shakeDetectionMethod == "Deslizar") {
                Log.i("Perdi", "Perdi con deslizar")
            }

            showToast("onScroll")
            return true
        }

    }

    private fun handleLoss() {
        if (!hasLost) {
            hasLost = true
            showToast("¡Perdiste! Puntaje obtenido: $score")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
