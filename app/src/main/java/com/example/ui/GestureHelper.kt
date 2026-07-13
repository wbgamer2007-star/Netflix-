package com.example.ui

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import kotlin.math.abs

class GestureHelper(
    private val context: Context,
    private val activity: Activity,
    private val onSeek: (Long) -> Unit,
    private val onVolumeChange: (Float) -> Unit,
    private val onBrightnessChange: (Float) -> Unit,
    private val onSingleTap: () -> Unit
) : GestureDetector.SimpleOnGestureListener() {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    
    private var isVolumeGesture = false
    private var isBrightnessGesture = false
    private var isSeekGesture = false
    
    private var startVolume = 0
    private var startBrightness = 0f

    override fun onDown(e: MotionEvent): Boolean {
        isVolumeGesture = false
        isBrightnessGesture = false
        isSeekGesture = false
        
        startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        startBrightness = activity.window.attributes.screenBrightness
        if (startBrightness < 0) startBrightness = 0.5f // Default
        
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        onSingleTap()
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (e1 == null) return false

        val deltaX = e2.x - e1.x
        val deltaY = e2.y - e1.y

        if (!isVolumeGesture && !isBrightnessGesture && !isSeekGesture) {
            if (abs(deltaX) > abs(deltaY)) {
                // Seek gesture
                isSeekGesture = true
            } else {
                if (e1.x > activity.resources.displayMetrics.widthPixels / 2) {
                    isVolumeGesture = true
                } else {
                    isBrightnessGesture = true
                }
            }
        }

        if (isVolumeGesture) {
            val delta = -deltaY / activity.resources.displayMetrics.heightPixels
            val newVolume = (startVolume + delta * maxVolume * 2).toInt().coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
            onVolumeChange(newVolume.toFloat() / maxVolume)
        } else if (isBrightnessGesture) {
            val delta = -deltaY / activity.resources.displayMetrics.heightPixels
            val newBrightness = (startBrightness + delta * 2).coerceIn(0f, 1f)
            val lp = activity.window.attributes
            lp.screenBrightness = newBrightness
            activity.window.attributes = lp
            onBrightnessChange(newBrightness)
        } else if (isSeekGesture) {
            // we could handle seeking here, or just let ExoPlayer handle it natively
        }

        return true
    }
}
