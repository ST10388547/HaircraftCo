package com.example.haircraftco

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class HairCraftCoApp : Application() {

    // 🔹 This makes sure the saved locale is applied before any Activity is created
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base, LocaleHelper.getLanguage(base)))
    }

    // 🔹 Add this method — it reapplies locale whenever configuration changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this))
    }

    override fun onCreate() {
        super.onCreate()

        // 🔹 Firestore settings
        val settings = FirebaseFirestoreSettings.Builder()
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}

