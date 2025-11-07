package com.example.haircraftco.models

data class Loyalty(
    val userId: String = "",
    val points: Int = 0,
    val rewards: List<String> = emptyList(),
    val badges: List<String> = emptyList()
)