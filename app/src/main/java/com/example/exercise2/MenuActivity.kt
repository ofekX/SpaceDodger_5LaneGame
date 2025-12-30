package com.example.exercise2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val fastSwitch = findViewById<SwitchMaterial>(R.id.fast_switch)

        val startButtonMode = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.start_button_mode_button)
        val startSensorMode = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.start_sensor_mode_button)

        val topTenButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.top_ten_button)

        startButtonMode.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FAST_MODE", fastSwitch.isChecked)
            intent.putExtra("CONTROL_MODE", "BUTTON")
            startActivity(intent)
        }

        startSensorMode.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FAST_MODE", fastSwitch.isChecked)
            intent.putExtra("CONTROL_MODE", "SENSOR")
            startActivity(intent)
        }

        topTenButton.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
    }
}
