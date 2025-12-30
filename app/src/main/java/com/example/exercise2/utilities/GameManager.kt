package com.example.exercise2.utilities
import kotlin.random.Random

class GameManager {
    val rows = 8
    val cols = 5

    var currentLane: Int = 2
    var lives: Int = 3

    private val obstacles = mutableListOf<Int>()

    private val coins = mutableListOf<Coin>()

    var coinsCollected: Int = 0
        private set


    var lastHit: Boolean = false
        private set
    var lastWasGameOver: Boolean = false
        private set

    private var stepCounter = 0

    var distance: Int = 0
        private set

    fun moveLeft() {
        if (currentLane > 0) currentLane--
    }

    fun moveRight() {
        if (currentLane < cols - 1) currentLane++
    }

    fun spawnInitialObstacles() {
        distance = 0
        obstacles.clear()
        lives = 3
        currentLane = 2
        lastHit = false
        lastWasGameOver = false
        stepCounter = 0
        coins.clear()
        coinsCollected = 0
        spawnAlien()
    }

    fun stepObstacles() {
        if (lives <= 0) {
            lastWasGameOver = true
            return
        }

        lastHit = false
        lastWasGameOver = false
        stepCounter++
        distance++

        var i = 0
        while (i < obstacles.size) {
            val pos = obstacles[i]
            val row = pos / cols
            val col = pos % cols
            val newRow = row + 1

            if (newRow >= rows) {
                obstacles.removeAt(i)
                continue
            }

            if (newRow == rows - 1 && col == currentLane) {
                lastHit = true
                lives--
                obstacles.removeAt(i)
                if (lives <= 0) {
                    lastWasGameOver = true
                }
                continue
            }

            obstacles[i] = newRow * cols + col
            i++
        }

        if (stepCounter % 2 == 0 && lives > 0) {
            spawnAlien()
        }

        stepCoins()
        spawnCoin()
    }

    private fun spawnAlien() {
        val col = Random.nextInt(cols)
        obstacles.add(col)
    }

    private fun isObstacleAt(row: Int, col: Int): Boolean {
        val pos = row * cols + col
        for (i in obstacles) {
            if (i == pos)
                return true
        }
        return false
    }

    private fun spawnCoin() {
        if (Random.nextInt(100) >= 25)
            return

        val col = Random.nextInt(cols)
        val row = 0

        if (isObstacleAt(row, col))
            return

        for (i in coins) {
            if (i.row == row && i.col == col)
                return
        }
        coins.add(Coin(row, col))
    }

    private fun stepCoins() {
        var i = 0
        while (i < coins.size) {
            val coin = coins[i]
            val nextRow = coin.row + 1

            if (nextRow == rows - 1 && coin.col == currentLane) {
                coinsCollected++
                coins.removeAt(i)
                continue
            }

            if (nextRow >= rows) {
                coins.removeAt(i)
                continue
            }

            coin.row = nextRow
            i++
        }
    }

    fun getObstaclesCount(): Int = obstacles.size
    fun getObstacleRow(index: Int): Int = obstacles[index] / cols
    fun getObstacleCol(index: Int): Int = obstacles[index] % cols

    fun getCoinsCount(): Int = coins.size
    fun getCoinRow(index: Int): Int = coins[index].row
    fun getCoinCol(index: Int): Int = coins[index].col
}
