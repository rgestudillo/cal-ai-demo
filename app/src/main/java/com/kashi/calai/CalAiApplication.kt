package com.kashi.calai


import android.app.Application
import com.google.firebase.FirebaseApp

class CalAiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}