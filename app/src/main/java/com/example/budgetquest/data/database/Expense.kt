package com.example.budgetquest.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val categoryId: Int,
    val amount: Double,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val photoPath: String? = null,
    val isIncome: Boolean = false
)