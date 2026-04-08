package com.example.budgetquest.data.repository

import android.content.Context
import com.example.budgetquest.data.database.*

class BudgetRepository(context: Context) {

    private val db = BudgetDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val expenseDao = db.expenseDao()
    private val goalDao = db.goalDao()

    // --- USER ---
    suspend fun register(user: User) = userDao.insertUser(user)
    suspend fun login(username: String, password: String) =
        userDao.login(username, password)
    suspend fun getUserByUsername(username: String) =
        userDao.getUserByUsername(username)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun getUserById(userId: Int) = userDao.getUserById(userId)

    // --- CATEGORIES ---
    suspend fun insertCategory(category: Category) =
        categoryDao.insertCategory(category)
    fun getCategories(userId: Int) =
        categoryDao.getCategoriesForUser(userId)
    suspend fun getCategoriesOnce(userId: Int) =
        categoryDao.getCategoriesOnce(userId)
    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category)

    // --- EXPENSES ---
    suspend fun insertExpense(expense: Expense) =
        expenseDao.insertExpense(expense)
    fun getAllExpenses(userId: Int) =
        expenseDao.getAllExpenses(userId)
    fun getExpensesByPeriod(userId: Int, start: String, end: String) =
        expenseDao.getExpensesByPeriod(userId, start, end)
    suspend fun getTotalPerCategory(userId: Int, start: String, end: String) =
        expenseDao.getTotalPerCategory(userId, start, end)
    suspend fun getTotalIncome(userId: Int) =
        expenseDao.getTotalIncome(userId)
    suspend fun getTotalExpenses(userId: Int) =
        expenseDao.getTotalExpenses(userId)

    // --- GOALS ---
    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)
    fun getGoals(userId: Int) = goalDao.getGoalsForUser(userId)
}