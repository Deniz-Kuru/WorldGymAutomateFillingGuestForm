package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileName: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val yearOfBirth: Int,
    val gender: String, // M or F
    val address: String,
    val apartment: String,
    val city: String,
    val stateProv: String,
    val postalCode: String,
    val phone: String,
    val preferredGymId: String
)
