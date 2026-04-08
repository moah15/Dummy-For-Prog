package com.example.budgetquest.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetquest.data.database.Goal
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.ActivityAddGoalBinding
import kotlinx.coroutines.launch

class AddGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddGoalBinding
    private lateinit var repository: BudgetRepository
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = BudgetRepository(this)
        val prefs = getSharedPreferences("BudgetQuestPrefs", MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        binding.btnSaveGoal.setOnClickListener {
            val name = binding.etGoalName.text.toString().trim()
            val amountText = binding.etTargetAmount.text.toString().trim()
            val date = binding.etTargetDate.text.toString().trim()

            if (name.isEmpty()) {
                binding.etGoalName.error = "Please enter a goal name"
                return@setOnClickListener
            }
            if (amountText.isEmpty()) {
                binding.etTargetAmount.error = "Please enter a target amount"
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                binding.etTargetDate.error = "Please enter a target date"
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.etTargetAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            val goal = Goal(
                userId = userId,
                name = name,
                targetAmount = amount,
                targetDate = date
            )

            lifecycleScope.launch {
                repository.insertGoal(goal)
                runOnUiThread {
                    Toast.makeText(this@AddGoalActivity, "Goal saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        binding.btnCancel.setOnClickListener { finish() }
    }
}