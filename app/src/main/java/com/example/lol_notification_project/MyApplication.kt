package com.example.lol_notification_project

import android.app.Application
import com.example.lol_notification_project.di.appModule
import com.example.lol_notification_project.di.mainViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
            modules(mainViewModelModule)
        }

    }

}