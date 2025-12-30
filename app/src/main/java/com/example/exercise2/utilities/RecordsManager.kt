package com.example.exercise2.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RecordsManager {

    private val gson = Gson()

    fun getTop10(): MutableList<Record> {
        val json = SharedPreferencesManager
            .getInstance()
            .getString(Constants.SP_KEYS.KEY_TOP10, "[]")

        val type = object : TypeToken<MutableList<Record>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveTop10(list: List<Record>) {
        val json = gson.toJson(list)
        SharedPreferencesManager.getInstance().putString(Constants.SP_KEYS.KEY_TOP10, json)
    }

    fun addRecord(newRecord: Record) {
        val list = getTop10()

        val alreadyExists = list.any { it.distance == newRecord.distance }
        if (alreadyExists) return

        list.add(newRecord)

        val sortedTop10 = list
            .sortedByDescending { it.distance }
            .take(10)

        saveTop10(sortedTop10)
    }

    fun wouldEnterTop10(distance: Int): Boolean {
        val list = getTop10()

        if (list.any { it.distance == distance }) return false

        if (list.size < 10) return true

        val worst = list.minOf { it.distance }
        return distance > worst
    }
}
