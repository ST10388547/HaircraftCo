package com.example.haircraftco

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.haircraftco.models.Booking

class ViewBookingsActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private val db = FirebaseFirestore.getInstance()
    private lateinit var listView: ListView
    private var bookingsList: MutableList<Booking> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bookings)

        listView = findViewById(R.id.lv_bookings)

        loadBookings()

        listView.setOnItemClickListener { _, _, position, _ ->
            val booking = bookingsList[position]
            AlertDialog.Builder(this)
                .setTitle("Manage Booking: ${booking.bookingId}")
                .setMessage("Status: ${booking.status}")
                .setPositiveButton("Approve") { _, _ ->
                    db.collection("bookings").document(booking.bookingId).update("status", "approved")
                        .addOnSuccessListener { loadBookings(); Toast.makeText(this, "Approved", Toast.LENGTH_SHORT).show() }
                }
                .setNegativeButton("Reject") { _, _ ->
                    db.collection("bookings").document(booking.bookingId).update("status", "rejected")
                        .addOnSuccessListener { loadBookings(); Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show() }
                }
                .setNeutralButton("Cancel", null)
                .show()
        }
    }

    private fun loadBookings() {
        db.collection("bookings").get()
            .addOnSuccessListener { documents ->
                bookingsList.clear()
                bookingsList.addAll(documents.toObjects(Booking::class.java))
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bookingsList.map {
                    "ID: ${it.bookingId} - User: ${it.userId} - Service: ${it.serviceId} - Date: ${it.date} - Status: ${it.status}"
                })
                listView.adapter = adapter
            }
    }
}