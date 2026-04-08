package com.example.budgetquest.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.budgetquest.R
import com.example.budgetquest.databinding.ActivityTransactionDetailBinding
import java.text.NumberFormat
import java.util.Locale

class TransactionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

        // Get data passed from TransactionAdapter
        val amount = intent.getDoubleExtra("amount", 0.0)
        val description = intent.getStringExtra("description") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val startTime = intent.getStringExtra("startTime") ?: ""
        val endTime = intent.getStringExtra("endTime") ?: ""
        val isIncome = intent.getBooleanExtra("isIncome", false)
        val photoPath = intent.getStringExtra("photoPath")

        binding.tvDetailAmount.text = format.format(amount)
        binding.tvDetailAmount.setTextColor(
            getColor(if (isIncome) R.color.income_green else R.color.expense_red)
        )
        binding.tvDetailType.text = if (isIncome) "Income" else "Expense"
        binding.tvDetailDescription.text = description
        binding.tvDetailDate.text = date
        binding.tvDetailTime.text = "$startTime — $endTime"

        // Show photo if exists
        if (!photoPath.isNullOrEmpty()) {
            binding.cardPhoto.visibility = View.VISIBLE
            Glide.with(this)
                .load(photoPath)
                .into(binding.ivDetailPhoto)
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}