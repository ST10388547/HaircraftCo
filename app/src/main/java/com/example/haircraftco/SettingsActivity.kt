package com.example.haircraftco

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var spLanguage: Spinner
    private lateinit var tvOfflineStatus: TextView
    private lateinit var btnApply: Button
    private lateinit var sharedPrefs: SharedPreferences

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base, LocaleHelper.getLanguage(base)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE)

        spLanguage = findViewById(R.id.sp_language)
        tvOfflineStatus = findViewById(R.id.tv_offline_status)
        btnApply = findViewById(R.id.btn_apply)

        // --- Language Spinner setup ---
        val languages = arrayOf("English", "isiZulu", "Afrikaans")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLanguage.adapter = adapter

        // --- Set spinner selection based on saved language ---
        val savedLang = sharedPrefs.getString("language", "en") ?: "en"
        spLanguage.setSelection(
            when (savedLang) {
                "zu" -> 1
                "af" -> 2
                else -> 0
            }
        )

        // --- Offline Mode Status ---
        updateOfflineStatus()

        // --- Apply button ---
        btnApply.setOnClickListener {
            val langCode = when (spLanguage.selectedItemPosition) {
                1 -> "zu"
                2 -> "af"
                else -> "en"
            }

            // ✅ Save selected language
            LocaleHelper.saveLanguage(this, langCode)

            // ✅ Apply locale immediately
            LocaleHelper.setLocale(this, langCode)

            // ✅ Restart app properly (NO Runtime.exit)
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finishAffinity() // closes all previous activities safely
        }
    }

    private fun updateOfflineStatus() {
        if (isOnline(this)) {
            tvOfflineStatus.text = getString(R.string.online_status)
            tvOfflineStatus.setTextColor(
                ContextCompat.getColor(this, android.R.color.holo_green_dark)
            )
        } else {
            tvOfflineStatus.text = getString(R.string.offline_status)
            tvOfflineStatus.setTextColor(
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
            )
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(net) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
