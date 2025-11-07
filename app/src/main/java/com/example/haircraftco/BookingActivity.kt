package com.example.haircraftco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.haircraftco.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class BookingActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedDate: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val calendarView = findViewById<CalendarView>(R.id.calendar)
        val bookBtn = findViewById<Button>(R.id.btn_book)

        val serviceId = intent.getStringExtra("serviceId") ?: ""

        // ✅ Modern date selection using Calendar
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0) // start of day
            cal.set(Calendar.MILLISECOND, 0)
            selectedDate = cal.time
        }

        bookBtn.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            val booking = Booking(
                bookingId = db.collection("bookings").document().id,
                userId = userId,
                serviceId = serviceId,
                stylistId = "default_stylist", // mock
                date = selectedDate,
                status = "pending"
            )

            db.collection("bookings").document(booking.bookingId)
                .set(booking)
                .addOnSuccessListener {
                    // Add loyalty points
                    db.collection("users").document(userId)
                        .update("loyaltyPoints", FieldValue.increment(10))

                    // Mock payment / proceed to checkout
                    startActivity(
                        Intent(this, CheckoutActivity::class.java)
                            .putExtra("bookingId", booking.bookingId)
                    )

                    Toast.makeText(this, "Booking created", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
