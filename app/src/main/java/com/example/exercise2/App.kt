package com.example.exercise2

import android.app.Application
import com.example.exercise2.utilities.SignalManager
import com.example.exercise2.utilities.SharedPreferencesManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SignalManager.init(this)
        SharedPreferencesManager.init(this)
    }
}
