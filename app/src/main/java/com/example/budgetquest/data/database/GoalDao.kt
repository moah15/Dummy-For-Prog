package com.example.budgetquest.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY targetDate ASC")
    fun getGoalsForUser(userId: Int): LiveData<List<Goal>>
}