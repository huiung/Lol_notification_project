package com.example.lol_notification_project

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Preferences {

    private lateinit var preferences: SharedPreferences

    fun setbool(context: Context, key: String, value: Boolean) {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun getbool(context: Context, key: String) : Boolean{
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        return preferences.getBoolean(key, false)
    }

    fun getAllKeys(context: Context) : MutableSet<String>{
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val arr = preferences.all.keys
        return arr
    }

    fun removebool(context: Context, key: String) {
        preferences = context.getSharedPreferences("SummonerID", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove(key)
        editor.commit()
    }

    fun setLong(context: Context, key: String, value: Long) {
        preferences = context.getSharedPreferences("matchInfo", Activity.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.commit()
    }

    fun getLong(context: Context, key: String) : Long {
        preferences = context.getSharedPreferences("matchInfo", Activity.MODE_PRIVATE)
        return preferences.getLong(key, -1) //default 값
    }

}