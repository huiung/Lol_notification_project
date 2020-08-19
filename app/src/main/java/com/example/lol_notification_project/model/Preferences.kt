package com.example.lol_notification_project.model

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object Preferences {

    private lateinit var preferences: SharedPreferences

    fun setAPI(context: Context, key: String, value: String) {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getAPI(context: Context, key: String) : String? {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        return preferences.getString(key, " ")
    }

    fun setBool(context: Context, key: String, value: Boolean) {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBool(context: Context, key: String) : Boolean {
        preferences = context.getSharedPreferences("API", Activity.MODE_PRIVATE)
        return preferences.getBoolean(key, false)
    }

    fun setString(context: Context, key: String, value: String) {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(context: Context, key: String) : String? {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        return preferences.getString(key, "NoID")
    }

    fun getAll(context: Context) : MutableMap<String, *>? {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val arr = preferences.all
        return arr
    }

    fun removeString(context: Context, key: String) {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun setLong(context: Context, key: String, value: Long) {
        preferences = context.getSharedPreferences("matchInfo", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(context: Context, key: String) : Long {
        preferences = context.getSharedPreferences("matchInfo", Activity.MODE_PRIVATE)
        return preferences.getLong(key, -1) //default 값
    }

}