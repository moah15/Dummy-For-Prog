package com.example.budgetquest.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetquest.data.database.Goal
import com.example.budgetquest.databinding.ItemGoalBinding
import java.text.NumberFormat
import java.util.Locale

class GoalAdapter(private var goals: List<Goal>) :
    RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class GoalViewHolder(val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        with(holder.binding) {
            tvGoalName.text = goal.name
            tvGoalDate.text = goal.targetDate
            tvCurrentAmount.text = "${format.format(goal.currentAmount)} saved"
            tvTargetAmount.text = "of ${format.format(goal.targetAmount)}"

            val percent = if (goal.targetAmount > 0) {
                ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
            } else 0

            progressGoal.progress = percent
            tvGoalPercent.text = "$percent%"
        }
    }

    override fun getItemCount() = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        goals = newGoals
        notifyDataSetChanged()
    }
}