package com.example.budgetquest.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: Int): LiveData<List<Expense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getExpensesByPeriod(
        userId: Int,
        startDate: String,
        endDate: String
    ): LiveData<List<Expense>>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE userId = :userId 
        AND isIncome = 0
        AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryId
    """)
    suspend fun getTotalPerCategory(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<CategoryTotal>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND isIncome = 1")
    suspend fun getTotalIncome(userId: Int): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND isIncome = 0")
    suspend fun getTotalExpenses(userId: Int): Double?
}

data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)