package com.example.budgetquest.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.budgetquest.data.database.Expense
import com.example.budgetquest.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(private var expenses: List<Expense>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val expense = expenses[position]
        with(holder.binding) {
            tvDescription.text = expense.description
            tvDate.text = expense.date
            tvTime.text = "${expense.startTime} - ${expense.endTime}"
            tvAmount.text = format.format(expense.amount)
            tvAmount.setTextColor(
                if (expense.isIncome)
                    holder.itemView.context.getColor(com.example.budgetquest.R.color.income_green)
                else
                    holder.itemView.context.getColor(com.example.budgetquest.R.color.expense_red)
            )

            // Show photo if available
            if (expense.photoPath != null) {
                ivPhoto.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(expense.photoPath)
                    .into(ivPhoto)
            } else {
                ivPhoto.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = expenses.size

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}