package com.chodocu.app

import android.app.Application
import com.chodocu.app.di.AppContainer

/** Lớp Application: tạo AppContainer một lần duy nhất khi app khởi động. */
class ChoDoCuApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
