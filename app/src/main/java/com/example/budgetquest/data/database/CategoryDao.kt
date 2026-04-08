package com.example.budgetquest.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesForUser(userId: Int): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesOnce(userId: Int): List<Category>

    @Delete
    suspend fun deleteCategory(category: Category)
}