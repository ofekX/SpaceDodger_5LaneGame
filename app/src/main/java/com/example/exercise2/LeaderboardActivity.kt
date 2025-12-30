package com.example.exercise2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.exercise2.interfaces.Callback_HighScoreClicked
import com.example.exercise2.fragments.HighScoreFragment
import com.example.exercise2.fragments.MapFragment

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var highScoreFragment: HighScoreFragment
    private lateinit var mapFragment: MapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        highScoreFragment = HighScoreFragment()
        mapFragment = MapFragment()

        highScoreFragment.callbackHighScoreClicked = object : Callback_HighScoreClicked {
            override fun zoom(lat: Double, lon: Double) {
                mapFragment.zoom(lat, lon)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.leaderboard_FRAME_table, highScoreFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.leaderboard_FRAME_map, mapFragment)
            .commit()
    }
}
