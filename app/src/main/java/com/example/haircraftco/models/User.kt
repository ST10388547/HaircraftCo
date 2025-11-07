package com.example.haircraftco.models

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photo: String = "",
    val loyaltyPoints: Int = 0,
    val isAdmin: Boolean = false
)