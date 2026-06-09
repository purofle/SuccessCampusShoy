package com.github.purofle.sandauschool

import android.app.Application
import android.content.Context
import com.github.purofle.sandauschool.data.setSharedContext

class MyApp : Application() {
    companion object {
        private lateinit var instance: MyApp

        fun getContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setSharedContext(this)
    }
}