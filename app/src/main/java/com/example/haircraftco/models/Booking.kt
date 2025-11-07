package com.example.haircraftco.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val stylistId: String = "",
    @ServerTimestamp val date: Date? = null,
    val status: String = ""
)