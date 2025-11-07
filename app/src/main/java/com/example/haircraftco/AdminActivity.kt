package com.example.haircraftco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val manageServicesBtn = findViewById<Button>(R.id.btn_manage_services)
        val viewBookingsBtn = findViewById<Button>(R.id.btn_view_bookings)

        manageServicesBtn.setOnClickListener {
            startActivity(Intent(this, ManageServicesActivity::class.java))
        }

        viewBookingsBtn.setOnClickListener {
            startActivity(Intent(this, ViewBookingsActivity::class.java))
        }
    }
}