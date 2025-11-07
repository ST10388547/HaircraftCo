package com.example.haircraftco

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context

class ProfileActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var bookingRecyclerView: RecyclerView
    private lateinit var noBookingsTv: TextView
    private lateinit var loadingBar: ProgressBar

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class Booking(
        val serviceName: String = "",
        val date: String = "",
        val status: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val nameEt = findViewById<EditText>(R.id.et_name)
        val phoneEt = findViewById<EditText>(R.id.et_phone)
        val updateBtn = findViewById<Button>(R.id.btn_update)

        bookingRecyclerView = findViewById(R.id.rv_bookings)
        noBookingsTv = findViewById(R.id.tv_no_bookings)
        loadingBar = findViewById(R.id.progress_loading)
        bookingRecyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid ?: return

        // Load profile info
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                nameEt.setText(document.getString("name"))
                phoneEt.setText(document.getString("phone"))
            }

        // Update profile info
        updateBtn.setOnClickListener {
            val updates = hashMapOf<String, Any>(
                "name" to nameEt.text.toString(),
                "phone" to phoneEt.text.toString()
            )
            db.collection("users").document(userId).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Load user bookings
        loadBookings(userId)
    }

    private fun loadBookings(userId: String) {
        val currentDateStr = dateFormat.format(Date())
        val bookingsList = mutableListOf<Booking>()

        loadingBar.visibility = View.VISIBLE
        noBookingsTv.visibility = View.GONE
        bookingRecyclerView.visibility = View.GONE

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    updateBookingsUI(emptyList(), currentDateStr)
                    return@addOnSuccessListener
                }

                val totalDocs = querySnapshot.size()
                var processedCount = 0

                for (document in querySnapshot) {
                    val serviceId = document.getString("serviceId") ?: ""
                    val timestamp = document.getTimestamp("date")
                    val dateStr = timestamp?.toDate()?.let { dateFormat.format(it) } ?: ""
                    val status = document.getString("status")?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    } ?: "Pending"

                    if (serviceId.isEmpty()) {
                        bookingsList.add(Booking("Unknown Service", dateStr, status))
                        processedCount++
                        if (processedCount == totalDocs) updateBookingsUI(bookingsList, currentDateStr)
                    } else {
                        db.collection("services").document(serviceId).get()
                            .addOnSuccessListener { serviceDoc ->
                                val serviceName = serviceDoc.getString("name") ?: "Unknown Service"
                                bookingsList.add(Booking(serviceName, dateStr, status))
                            }
                            .addOnFailureListener {
                                bookingsList.add(Booking("Unknown Service", dateStr, status))
                            }
                            .addOnCompleteListener {
                                processedCount++
                                if (processedCount == totalDocs) updateBookingsUI(bookingsList, currentDateStr)
                            }
                    }
                }
            }
            .addOnFailureListener {
                loadingBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load bookings: ${it.message}", Toast.LENGTH_SHORT).show()
                updateBookingsUI(emptyList(), currentDateStr)
            }
    }

    private fun updateBookingsUI(bookingsList: List<Booking>, currentDateStr: String) {
        loadingBar.visibility = View.GONE
        if (bookingsList.isEmpty()) {
            noBookingsTv.visibility = View.VISIBLE
            bookingRecyclerView.visibility = View.GONE
        } else {
            noBookingsTv.visibility = View.GONE
            bookingRecyclerView.visibility = View.VISIBLE
            val pastBookings = bookingsList.filter { it.date < currentDateStr }
            val upcomingBookings = bookingsList.filter { it.date >= currentDateStr }
            bookingRecyclerView.adapter = BookingAdapter(pastBookings, upcomingBookings)
        }
    }
}

// --- BookingAdapter.kt ---
class BookingAdapter(
    private val pastBookings: List<ProfileActivity.Booking>,
    private val upcomingBookings: List<ProfileActivity.Booking>
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceNameTv: TextView = itemView.findViewById(R.id.text_service_name)
        val dateTv: TextView = itemView.findViewById(R.id.text_date)
        val statusTv: TextView = itemView.findViewById(R.id.text_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = if (position < pastBookings.size) {
            pastBookings[position]
        } else {
            upcomingBookings[position - pastBookings.size]
        }
        holder.serviceNameTv.text = booking.serviceName
        holder.dateTv.text = booking.date
        holder.statusTv.text = booking.status
    }

    override fun getItemCount(): Int = pastBookings.size + upcomingBookings.size
}
