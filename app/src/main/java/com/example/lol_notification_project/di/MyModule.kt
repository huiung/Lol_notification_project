package com.example.lol_notification_project.di

import com.example.lol_notification_project.data.remote.SummonerAPI
import com.example.lol_notification_project.ui.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {


    single {
        Retrofit.Builder()
            .baseUrl("https://kr.api.riotgames.com/lol/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SummonerAPI::class.java)
    }
}

val mainViewModelModule = module {
    viewModel { MainViewModel(get(), androidContext()) }
}