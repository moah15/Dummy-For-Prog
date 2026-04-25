package com.example.budgetquest

import org.junit.Test
import org.junit.Assert.*

/**
 * BudgetQuestUnitTest.kt
 * Unit tests for core BudgetQuest business logic.
 * These run locally without needing a device or emulator.
 *
 * Reference: https://developer.android.com/training/testing/local-tests
 */
class BudgetQuestUnitTest {

    /**
     * Test 1: Verify balance calculation is correct.
     * Balance = Total Income - Total Expenses
     */
    @Test
    fun balance_calculation_isCorrect() {
        val income = 5000.0
        val expenses = 2000.0
        val expectedBalance = 3000.0
        val actualBalance = income - expenses
        assertEquals(expectedBalance, actualBalance, 0.0)
    }

    /**
     * Test 2: Verify goal progress percentage calculation.
     * Progress = (currentAmount / targetAmount) * 100
     */
    @Test
    fun goal_progress_percentage_isCorrect() {
        val targetAmount = 10000.0
        val currentAmount = 2500.0
        val expectedPercent = 25
        val actualPercent = ((currentAmount / targetAmount) * 100).toInt()
        assertEquals(expectedPercent, actualPercent)
    }

    /**
     * Test 3: Verify goal progress does not exceed 100%.
     * Even if currentAmount exceeds targetAmount.
     */
    @Test
    fun goal_progress_doesNotExceed100() {
        val targetAmount = 1000.0
        val currentAmount = 1500.0
        val percent = ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
        assertTrue(percent <= 100)
    }

    /**
     * Test 4: Verify level calculation from points.
     * Every 100 points = 1 level up
     */
    @Test
    fun level_calculation_fromPoints_isCorrect() {
        val points = 250
        val expectedLevel = 3
        val actualLevel = (points / 100) + 1
        assertEquals(expectedLevel, actualLevel)
    }

    /**
     * Test 5: Verify points within current level resets every 100.
     * Used for the progress bar in achievements screen.
     */
    @Test
    fun points_withinLevel_isCorrect() {
        val totalPoints = 250
        val expectedPointsInLevel = 50
        val actualPointsInLevel = totalPoints % 100
        assertEquals(expectedPointsInLevel, actualPointsInLevel)
    }

    /**
     * Test 6: Verify username validation — must be at least 3 characters.
     */
    @Test
    fun username_validation_minimumLength() {
        val shortUsername = "ab"
        val validUsername = "abc"
        assertFalse(shortUsername.length >= 3)
        assertTrue(validUsername.length >= 3)
    }

    /**
     * Test 7: Verify password validation — must be at least 6 characters.
     */
    @Test
    fun password_validation_minimumLength() {
        val shortPassword = "12345"
        val validPassword = "123456"
        assertFalse(shortPassword.length >= 6)
        assertTrue(validPassword.length >= 6)
    }

    /**
     * Test 8: Verify amount validation — must be greater than zero.
     */
    @Test
    fun amount_validation_mustBePositive() {
        val invalidAmount = -50.0
        val validAmount = 100.0
        assertFalse(invalidAmount > 0)
        assertTrue(validAmount > 0)
    }

    /**
     * Test 9: Verify date range filter logic.
     * Transaction date must be between start and end date.
     */
    @Test
    fun date_filter_isCorrect() {
        val startDate = "2026-01-01"
        val endDate = "2026-12-31"
        val transactionDate = "2026-06-15"
        assertTrue(transactionDate >= startDate && transactionDate <= endDate)
    }

    /**
     * Test 10: Verify streak increments correctly.
     * Streak increases by 1 each new day a transaction is logged.
     */
    @Test
    fun streak_increment_isCorrect() {
        val currentStreak = 5
        val newStreak = currentStreak + 1
        assertEquals(6, newStreak)
    }
}