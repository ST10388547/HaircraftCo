package com.example.haircraftco.models

data class Payment(
    val paymentId: String = "",
    val bookingId: String = "",
    val amount: Double = 0.0,
    val method: String = "",
    val status: String = ""
)