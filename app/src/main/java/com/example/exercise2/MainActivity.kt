package com.example.exercise2

import com.example.exercise2.utilities.GameManager
import com.example.exercise2.utilities.SignalManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Timer
import java.util.TimerTask
import com.example.exercise2.interfaces.TiltCallback
import com.example.exercise2.utilities.TiltDetector
import com.example.exercise2.utilities.SingleSoundPlayer
import com.example.exercise2.utilities.Record
import com.example.exercise2.utilities.RecordsManager
import android.app.AlertDialog
import android.widget.EditText
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager

    private lateinit var spaceshipCells: Array<ImageView?>
    private lateinit var obstacleCells: Array<Array<ImageView?>>

    private var leftButton: ImageButton? = null
    private var rightButton: ImageButton? = null

    private val hearts = arrayOfNulls<ImageView>(3)

    private lateinit var timer: Timer
    private var timerOn: Boolean = false

    private var tickRate = 700L
    companion object {
        const val EXTRA_FAST_MODE = "FAST_MODE"
        const val EXTRA_CONTROL_MODE = "CONTROL_MODE"
    }

    private lateinit var distanceText: TextView
    private lateinit var coinsText: TextView

    private var controlMode: String = "BUTTON"
    private var tiltDetector: TiltDetector? = null

    private lateinit var crashPlayer: SingleSoundPlayer

    private var recordSaved = false

    private var pendingDistance: Int? = null

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) saveRecordWithLocation()
            else saveRecordFallback()
        }

    private var pendingName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        gameManager = GameManager()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recordSaved = false

        crashPlayer = SingleSoundPlayer(this)

        distanceText = findViewById(R.id.txt_distance)
        updateDistance()

        coinsText = findViewById(R.id.txt_coins)
        updateCoins()

        val fastMode = intent.getBooleanExtra(EXTRA_FAST_MODE, false)
        tickRate = if (fastMode) 350L else 700L

        spaceshipCells = arrayOfNulls(gameManager.cols)
        obstacleCells = Array(gameManager.rows) { arrayOfNulls<ImageView>(gameManager.cols) }

        linkSpaceshipCells()
        linkObstacleCells()

        leftButton = findViewById(R.id.left_arrow_button)
        rightButton = findViewById(R.id.right_arrow_button)

        setupControlMode()

        updateSpaceship()

        leftButton?.setOnClickListener {
            gameManager.moveLeft()
            updateSpaceship()
        }

        rightButton?.setOnClickListener {
            gameManager.moveRight()
            updateSpaceship()
        }

        hideAllObstacles()

        hearts[0] = findViewById(R.id.heart3)
        hearts[1] = findViewById(R.id.heart2)
        hearts[2] = findViewById(R.id.heart1)
        updateHearts()

        spawnInitialObstacles()
        startGameLoop()
    }

    private fun updateDistance() {
        distanceText.text = "Distance: ${gameManager.distance}m"
    }

    private fun updateCoins() {
        coinsText.text = "Coins: ${gameManager.coinsCollected}"
    }

    private fun updateSpaceship() {
        val lane = gameManager.currentLane
        for (i in spaceshipCells.indices) {
            val cell = spaceshipCells[i]
            if (i == lane) {
                cell?.visibility = View.VISIBLE
            } else {
                cell?.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateHearts() {
        val lives = gameManager.lives
        for (i in hearts.indices) {
            val heart = hearts[i]
            if (i < lives) {
                heart?.visibility = View.VISIBLE
            } else {
                heart?.visibility = View.INVISIBLE
            }
        }
    }

    private fun linkSpaceshipCells() {
        for (lane in 0 until gameManager.cols) {
            val idName = "spaceship_cell_${lane + 1}"
            val id = resources.getIdentifier(idName, "id", packageName)
            spaceshipCells[lane] = findViewById(id)
        }
    }

    private fun linkObstacleCells() {
        var index = 1
        for (r in 0 until gameManager.rows) {
            for (c in 0 until gameManager.cols) {
                val idName = "mat_cell_$index"
                val id = resources.getIdentifier(idName, "id", packageName)
                obstacleCells[r][c] = findViewById(id)
                index++
            }
        }
    }

    private fun hideAllObstacles() {
        for (r in 0 until gameManager.rows) {
            for (c in 0 until gameManager.cols) {
                obstacleCells[r][c]?.visibility = View.INVISIBLE
            }
        }
    }


    private fun spawnInitialObstacles() {
        gameManager.spawnInitialObstacles()

        for (i in 0 until gameManager.getObstaclesCount()) {
            val row = gameManager.getObstacleRow(i)
            val col = gameManager.getObstacleCol(i)
            if (row in 0 until gameManager.rows && col in 0 until gameManager.cols) {
                obstacleCells[row][col]?.visibility = View.VISIBLE
            }
        }
    }

    private fun gameTick() {
        gameManager.stepObstacles()
        updateDistance()
        updateCoins()

        clearGrid()

        for (i in 0 until gameManager.getObstaclesCount()) {
            val row = gameManager.getObstacleRow(i)
            val col = gameManager.getObstacleCol(i)
            if (row in 0 until gameManager.rows && col in 0 until gameManager.cols) {
                drawAlien(row, col)
            }
        }

        for (i in 0 until gameManager.getCoinsCount()) {
            val row = gameManager.getCoinRow(i)
            val col = gameManager.getCoinCol(i)
            if (row in 0 until gameManager.rows && col in 0 until gameManager.cols) {
                if (obstacleCells[row][col]?.visibility != View.VISIBLE) {
                    drawCoin(row, col)
                }
            }
        }

        updateSpaceship()
        updateHearts()
        if (gameManager.lastHit) {
            crashPlayer.playSound(R.raw.crash)
            SignalManager.vibrate()

            if (gameManager.lastWasGameOver) {

                if (!recordSaved) {
                    recordSaved = true

                    val distance = gameManager.distance

                    if (RecordsManager.wouldEnterTop10(distance)) {
                        showNameDialog(distance)
                    } else {
                        goToMenuAfterDelay(3000)
                    }
                }

                SignalManager.toast("Game Over", long = true)
                if (timerOn) {
                    timer.cancel()
                    timerOn = false
                }
                leftButton?.isEnabled = false
                rightButton?.isEnabled = false

            } else {
                SignalManager.toast("Crash!")
            }


        }
    }

    private fun startGameLoop() {
        if (!timerOn) {
            timerOn = true
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        gameTick()
                    }
                }
            }, 0, tickRate)
        }
    }

    private fun setupControlMode() {
        controlMode = intent.getStringExtra(EXTRA_CONTROL_MODE) ?: "BUTTON"

        if (controlMode == "SENSOR") {
            leftButton?.visibility = View.INVISIBLE
            rightButton?.visibility = View.INVISIBLE
            leftButton?.isEnabled = false
            rightButton?.isEnabled = false

            tiltDetector = TiltDetector(this, object : TiltCallback {
                override fun tiltX() {
                    val x = tiltDetector?.lastX ?: return

                    if (x > 0) {
                        gameManager.moveLeft()
                    } else {
                        gameManager.moveRight()
                    }

                    updateSpaceship()
                }
                override fun tiltY() {
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (controlMode == "SENSOR") {
            tiltDetector?.start()
        }
        if (!gameManager.lastWasGameOver) {
            startGameLoop()
        }
    }

    override fun onPause() {
        super.onPause()
        if (controlMode == "SENSOR") {
            tiltDetector?.stop()
        }
        if (timerOn) {
            timer.cancel()
            timerOn = false
        }
    }

    private fun clearGrid() {
        for (r in 0 until gameManager.rows) {
            for (c in 0 until gameManager.cols) {
                obstacleCells[r][c]?.setImageDrawable(null)
                obstacleCells[r][c]?.visibility = View.INVISIBLE
            }
        }
    }

    private fun drawAlien(row: Int, col: Int) {
        obstacleCells[row][col]?.setImageResource(R.drawable.ufo)
        obstacleCells[row][col]?.visibility = View.VISIBLE
    }

    private fun drawCoin(row: Int, col: Int) {
        obstacleCells[row][col]?.setImageResource(R.drawable.coin)
        obstacleCells[row][col]?.visibility = View.VISIBLE
    }

    private fun saveRecordWithLocation() {
        val distance = pendingDistance ?: return
        val name = pendingName ?: "Player"

        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(this)
        val token = CancellationTokenSource()

        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
            .addOnSuccessListener { loc ->
                val lat = loc?.latitude ?: 0.0
                val lon = loc?.longitude ?: 0.0
                RecordsManager.addRecord(Record(distance, name, lat, lon))
                pendingDistance = null
                pendingName = null
                goToMenu()
            }
            .addOnFailureListener {
                saveRecordFallback()
            }
    }

    private fun saveRecordFallback() {
        val distance = pendingDistance ?: return
        val name = pendingName ?: "Player"

        RecordsManager.addRecord(Record(distance, name, 0.0, 0.0))
        pendingDistance = null
        pendingName = null
        goToMenu()
    }

    private fun showNameDialog(distance: Int) {
        val input = EditText(this)
        input.hint = "Name"

        AlertDialog.Builder(this)
            .setTitle("New High Score!")
            .setMessage("Please enter name")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                pendingDistance = distance
                pendingName = input.text.toString().trim().ifBlank { "Player" }
                saveRecordWithLocation()
            }
            .show()
    }

    private fun goToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun goToMenuAfterDelay(delayMs: Long) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread { goToMenu() }
            }
        }, delayMs)
    }
}
