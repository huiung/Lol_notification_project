package com.example.lol_notification_project.Retrofit2

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var instance : Retrofit? = null
    private val gson = GsonBuilder().setLenient().create()

    fun getInstnace() : Retrofit {
        if(instance == null){
            instance = Retrofit.Builder()
                .baseUrl("https://kr.api.riotgames.com/lol/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return instance!!
    }
}