package com.example.firebaseauthtest

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class FirebaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase Auth
        val auth = FirebaseAuth.getInstance()
        
        println("DEBUG: Firebase initialized for production use")
    }
}
