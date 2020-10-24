package com.example.lol_notification_project.data.local

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class Preferences(private val context: Context) {

    private lateinit var preferences: SharedPreferences

    fun setAPI(key: String, value: String) {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getAPI(key: String) : String? {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        return preferences.getString(key, " ")
    }

    fun setBool(key: String, value: Boolean) {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBool(key: String) : Boolean {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        return preferences.getBoolean(key, false)
    }

    fun setString(key: String, value: String) {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String) : String? {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        return preferences.getString(key, "NoID")
    }

    fun getAll() : MutableMap<String, *>? {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val arr = preferences.all
        return arr
    }

    fun removeString(key: String) {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun setLong(key: String, value: Long) {
        preferences = context.getSharedPreferences("matchInfo", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String) : Long {
        preferences = context.getSharedPreferences("matchInfo", Activity.MODE_PRIVATE)
        return preferences.getLong(key, -1) //default 값
    }

}