package com.example.budgetquest.ui.main

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetquest.data.database.Goal
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.ActivityAddGoalBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class AddGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddGoalBinding
    private lateinit var repository: BudgetRepository
    private var userId = -1
    private var selectedDate = ""

    companion object {
        private const val TAG = "AddGoalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = BudgetRepository(this)
        val prefs = getSharedPreferences("BudgetQuestPrefs", MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        Log.d(TAG, "AddGoalActivity started for userId: $userId")

        setupDatePicker()
        setupSaveButton()

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        // Open date picker when user taps the date field
        binding.etTargetDate.setOnClickListener {
            showDatePicker()
        }
        binding.etTargetDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.etTargetDate.setText(selectedDate)
                Log.d(TAG, "Date selected: $selectedDate")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupSaveButton() {
        binding.btnSaveGoal.setOnClickListener {
            val name = binding.etGoalName.text.toString().trim()
            val amountText = binding.etTargetAmount.text.toString().trim()
            val date = binding.etTargetDate.text.toString().trim()

            // Validate all fields
            if (name.isEmpty()) {
                binding.etGoalName.error = "Please enter a goal name"
                return@setOnClickListener
            }
            if (amountText.isEmpty()) {
                binding.etTargetAmount.error = "Please enter a target amount"
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                binding.etTargetDate.error = "Please select a target date"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.etTargetAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            if (userId == -1) {
                Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = Goal(
                userId = userId,
                name = name,
                targetAmount = amount,
                currentAmount = 0.0,
                targetDate = date,
                isCompleted = false
            )

            Log.d(TAG, "Saving goal: $name, amount: $amount, date: $date, userId: $userId")

            lifecycleScope.launch {
                try {
                    repository.insertGoal(goal)
                    Log.d(TAG, "Goal saved successfully")
                    runOnUiThread {
                        Toast.makeText(
                            this@AddGoalActivity,
                            "Goal saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving goal: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(
                            this@AddGoalActivity,
                            "Error saving goal: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}