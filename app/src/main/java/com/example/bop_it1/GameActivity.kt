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
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import java.util.*


class GameActivity : AppCompatActivity(), SensorEventListener {

    //Sensors
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private lateinit var gestureDetector: GestureDetector
    private val gravedadshakeThreshold = 10.0f
    private var lastShakeTimeAccelerometer: Long = 0
    private var lastShakeTimeOnScroll: Long = 0
    private var lastSwipetime: Long = 0
    private var lastBopiptime: Long = 0

    //Shared from setting
    private var shakeDetectionMethod: String = "Acelerometro"
    var shakeThreshold = 5.0f + gravedadshakeThreshold
    var rotationThreshold = 2.0f
    var difficultySettings = 5

    //Random instruction
    private lateinit var instructionTextView: TextView
    private val eventInstructions = arrayOf("Bop It", "Gira", "Desliza")

    //Handler and time for action
    private val handler = Handler()
    private var instructionChangeDelay: Long = 7000 // 7 segundos
    private var timeRemaining: Long = instructionChangeDelay
    private lateinit var timeRemainingTextView: TextView

    private val updateInstructionRunnable = object : Runnable {
        override fun run() {
            setRandomInstruction()
            //updateTimer()
            handler.postDelayed(this, timeRemaining)
        }
    }

    //Score
    private lateinit var scoreTextView: TextView
    private var score: Int = 0
    private var maxScore: Int = 100

    //Sound
    private lateinit var backgroundMediaPlayer: MediaPlayer
    private lateinit var fxMediaPlayer: MediaPlayer
    private lateinit var playbackParams: PlaybackParams
    private var volume: Float = 12.0f

    //Variables para controlar el estado del juego
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

        //textos
        timeRemainingTextView = findViewById(R.id.timeRemainingTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        playbackParams = backgroundMediaPlayer.playbackParams
        instructionTextView = findViewById(R.id.instructionTextView)

        //confifuracion sensores
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


        handler.post(updateInstructionRunnable)

        //SharedPreferences conectada con settings
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        shakeThreshold = sharedPreferences.getString("shake_threshold", "2.0")!!.toFloat()
        rotationThreshold = sharedPreferences.getString("rotation_threshold", "1.0")!!.toFloat()
        shakeDetectionMethod =
            sharedPreferences.getString("shake_detection_method", "Acelerometro") ?: "Acelerometro"
        difficultySettings = sharedPreferences.getString("difficulty_Settings", "1")!!.toInt()

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

        if (acceleration > shakeThreshold && System.currentTimeMillis() - lastShakeTimeAccelerometer > 1000) {
            lastShakeTimeAccelerometer = System.currentTimeMillis()

            if (instructionTextView.text.toString() == "Desliza" && shakeDetectionMethod == "Acelerometro") {
                setMusicOnFXMP(R.raw.corret)
                setRandomInstruction()
                updateScore(100)
                restartTimer()
            } else if (instructionTextView.text.toString() != "Desliza" && shakeDetectionMethod == "Acelerometro" && System.currentTimeMillis() - lastShakeTimeAccelerometer > 1000) {
                Log.i("Perdi", "Perdi con acelerometro")
                handleLoss()
            }

            showToast("Device Shaken!")
        }
    }

    private fun handleGyroscopeEvent(event: SensorEvent) {
        val xRotation = event.values[0]
        val yRotation = event.values[1]
        val zRotation = event.values[2]

        if (instructionTextView.text.toString() == "Gira" && System.currentTimeMillis() - lastSwipetime > 1000 &&
            (Math.abs(xRotation) > rotationThreshold || Math.abs(yRotation) > rotationThreshold || Math.abs(
                zRotation
            ) > rotationThreshold)
        ) {
            lastSwipetime = System.currentTimeMillis()
            setMusicOnFXMP(R.raw.corret)
            showToast("Device Rotated!")
            setRandomInstruction()
            updateScore(100)
            restartTimer()
        } else if (instructionTextView.text.toString() != "Gira" && System.currentTimeMillis() - lastSwipetime > 1000 &&
            (Math.abs(xRotation) > rotationThreshold || Math.abs(yRotation) > rotationThreshold || Math.abs(
                zRotation
            ) > rotationThreshold)
        ) {
            handleLoss()
            Log.i("Perdi", "Perdi con giroscopio")
            Log.i("Perdi x", "Perdi con giroscopio x: " + Math.abs(xRotation))
            Log.i("Perdi y", "Perdi con giroscopio y: " + Math.abs(yRotation))
            Log.i("Perdi z", "Perdi con giroscopio z: " + Math.abs(zRotation))
            Log.i("Perdi var", "Perdi con giroscopio var: " + rotationThreshold)
        }
    }
    private fun updateTimer() {
        timeRemaining -= 1000 // Resta 1 segundo (1000 milisegundos) en cada iteración
        timeRemainingTextView.text = "Tiempo restante: ${(timeRemaining / 1000)}"

        if (timeRemaining <= 0) {
            // Aquí puedes manejar el caso cuando se agota el tiempo (por ejemplo, finalizar el juego)
            handleLoss()
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

            if (instructionTextView.text.toString() == "Bop It" && System.currentTimeMillis() - lastBopiptime > 1000) {

                // Verificación adicional para "Bop It"
                lastBopiptime = System.currentTimeMillis()
                setMusicOnFXMP(R.raw.corret)
                setRandomInstruction()
                updateScore(100) // Aumentar el puntaje en 100 puntos
                restartTimer()
            } else if (instructionTextView.text.toString() != "Bop It" && System.currentTimeMillis() - lastBopiptime > 1000) {
                Log.i("Perdi", "Perdi con un toque")
                handleLoss()
            }
            showToast("onSingleTapUp")
            return true
        }

        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent,
            distanceX: Float, distanceY: Float
        ): Boolean {
            Log.i("DesliceGiroscopio", "DesliceGiroscopio: " + shakeDetectionMethod)

            if (instructionTextView.text.toString() == "Desliza" && shakeDetectionMethod == "Deslizar" && System.currentTimeMillis() - lastShakeTimeOnScroll > 1000) {

                Log.i("DesliceGiroscopio", "DesliceGiroscopio: " + shakeDetectionMethod)
                // Verificación adicional para "Bop It"
                lastShakeTimeOnScroll = System.currentTimeMillis()
                setMusicOnFXMP(R.raw.corret)
                setRandomInstruction()
                updateScore(100) // Aumentar el puntaje en 100 puntos
                restartTimer()
            } else if (instructionTextView.text.toString() != "Desliza" && shakeDetectionMethod == "Deslizar" && System.currentTimeMillis() - lastShakeTimeOnScroll > 1000) {
                Log.i("Perdi", "Perdi con deslizar")
                handleLoss()
            }
            showToast("onScroll")
            return true
        }
    }
    private fun showResultDialog() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val highScore = sharedPreferences.getInt("high_score", 0)

        val resultMessage: String
        if (score > highScore) {
            resultMessage = "¡Nuevo récord!\nPuntaje: $score"
            with(sharedPreferences.edit()) {
                putInt("high_score", score)
                apply()
            }

            showNewHighScoreDialog()
        } else {
            resultMessage = "Puntaje obtenido: $score"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Resultados")
        alertDialogBuilder.setMessage(resultMessage)
        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            finish()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showNewHighScoreDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("¡Nuevo récord!")
        alertDialogBuilder.setMessage("¡Felicidades! Has alcanzado un nuevo récord.")

        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            // Puedes agregar acciones adicionales después de presionar OK si es necesario
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun handleLoss() {
        if (!hasLost) {
            hasLost = true
            backgroundMediaPlayer.stop()
            fxMediaPlayer = MediaPlayer.create(this, R.raw.error)
            fxMediaPlayer.start()

            showResultDialog()
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
