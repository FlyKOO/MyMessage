package com.example.mymessage

import android.app.Application
import com.example.mymessage.di.AppDependencies

class MyMessageApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDependencies.initialize(this)
    }
}
