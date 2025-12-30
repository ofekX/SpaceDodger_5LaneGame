package com.example.exercise2.utilities

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast

object SignalManager {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun toast(text: String, long: Boolean = false) {
        val length = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(appContext, text, length).show()
    }

    fun vibrate(durationMs: Long = 150L) {
        val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }
}
