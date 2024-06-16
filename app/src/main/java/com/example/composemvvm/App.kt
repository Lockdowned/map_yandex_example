package com.example.composemvvm

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.yandex.mapkit.MapKitFactory

@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}