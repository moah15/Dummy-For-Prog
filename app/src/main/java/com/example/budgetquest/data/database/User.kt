package com.example.budgetquest.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val points: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastLoginDate: String = ""
)