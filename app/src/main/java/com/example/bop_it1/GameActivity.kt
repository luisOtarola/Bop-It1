package com.example.bop_it1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.widget.Toast

class GameActivity : AppCompatActivity() {
    private lateinit var backgroundMediaPlayer: MediaPlayer
    private lateinit var fxMediaPlayer: MediaPlayer
    private lateinit var playbackParams: PlaybackParams
    private var volume: Float = 0.9f
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gestureDetector = GestureDetector(this, GestureListener())

        backgroundMediaPlayer = MediaPlayer.create(this, R.raw.background)
        backgroundMediaPlayer.isLooping = true
        backgroundMediaPlayer.start()
        backgroundMediaPlayer.setVolume(volume, volume)

        playbackParams = backgroundMediaPlayer.playbackParams

        val buttonWin: Button = findViewById(R.id.btn_win)
        val buttonLose: Button = findViewById(R.id.btn_lose)
        val buttonLessVolume: Button = findViewById(R.id.btn_less)
        val buttonMoreVolume: Button = findViewById(R.id.btn_more)

        buttonWin.setOnClickListener() {
            setMusicOnFXMP(R.raw.corret)
        }

        buttonLose.setOnClickListener() {
            setMusicOnFXMP(R.raw.error)
        }

        buttonLessVolume.setOnClickListener() {
            if (volume >= 0.1f) {
                volume -= 0.1f
                backgroundMediaPlayer.setVolume(volume, volume)
            }
        }

        buttonMoreVolume.setOnClickListener() {
            if (volume <= 0.9f) {
                volume += 0.1f
                backgroundMediaPlayer.setVolume(volume, volume)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        backgroundMediaPlayer.pause()
        fxMediaPlayer.stop()
    }

    override fun onResume() {
        super.onResume()
        backgroundMediaPlayer.start()
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
/*
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return super.onScroll(e1, e2, distanceX, distanceY)
        }*/

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
