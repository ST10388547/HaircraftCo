package com.example.haircraftco.models

data class Service(
    val serviceId: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val duration: Int = 0
)