package com.example.haircraftco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.haircraftco.models.Service

class ServicesActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val listView = findViewById<ListView>(R.id.lv_services)

        db.collection("services").get()
            .addOnSuccessListener { documents ->
                val services = documents.toObjects(Service::class.java)
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, services.map { "${it.name} - R${it.price}" })
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    // Go to booking with selected service
                    val intent = Intent(this, BookingActivity::class.java)
                    intent.putExtra("serviceId", services[position].serviceId)
                    startActivity(intent)
                }
            }
    }
}