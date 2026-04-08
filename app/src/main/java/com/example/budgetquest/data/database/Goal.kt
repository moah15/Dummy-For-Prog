package com.example.budgetquest.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: String,
    val isCompleted: Boolean = false,
    val minMonthlyGoal: Double = 0.0,
    val maxMonthlyGoal: Double = 0.0
)