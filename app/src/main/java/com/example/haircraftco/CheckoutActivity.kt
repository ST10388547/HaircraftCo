package com.example.haircraftco

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.haircraftco.models.Payment

class CheckoutActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val summaryTv = findViewById<TextView>(R.id.tv_summary)
        val payBtn = findViewById<Button>(R.id.btn_pay)

        val bookingId = intent.getStringExtra("bookingId") ?: ""

        // Mock summary
        summaryTv.text = "Booking ID: $bookingId\nTotal: R100"

        payBtn.setOnClickListener {
            val payment = Payment(
                paymentId = db.collection("payments").document().id,
                bookingId = bookingId,
                amount = 100.0,
                method = "PayFast", // Mock
                status = "completed"
            )
            db.collection("payments").document(payment.paymentId).set(payment)
                .addOnSuccessListener {
                    Toast.makeText(this, "Payment mocked successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}