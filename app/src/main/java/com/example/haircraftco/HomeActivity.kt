package com.example.haircraftco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.haircraftco.models.User

class HomeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val loyaltyTv = findViewById<TextView>(R.id.tv_loyalty_balance)
        val servicesBtn = findViewById<Button>(R.id.btn_services)
        val bookingBtn = findViewById<Button>(R.id.btn_booking)
        val profileBtn = findViewById<Button>(R.id.btn_profile)
        val adminBtn = findViewById<Button>(R.id.btn_admin)
        val logoutBtn = findViewById<Button>(R.id.btn_logout)
        val btnSettings = findViewById<Button>(R.id.btn_settings)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    loyaltyTv.text = "Loyalty Balance: ${user?.loyaltyPoints} pts"
                    if (user?.isAdmin == true) {
                        adminBtn.visibility = View.VISIBLE
                    } else {
                        adminBtn.visibility = View.GONE
                    }
                }
        }

        servicesBtn.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }

        bookingBtn.setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        adminBtn.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}