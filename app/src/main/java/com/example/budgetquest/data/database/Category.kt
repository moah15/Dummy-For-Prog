package com.example.budgetquest.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val iconEmoji: String = "📁",
    val colorHex: String = "#00D4AA"
)