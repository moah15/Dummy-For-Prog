package com.example.budgetquest.ui.main

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetquest.data.database.Expense
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.FragmentTransactionsBinding
import java.util.Calendar

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: BudgetRepository
    private lateinit var adapter: TransactionAdapter
    private var userId = -1

    private var allExpenses = listOf<Expense>()
    private var currentFilter = "ALL"
    private var startDateFilter = ""
    private var endDateFilter = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = BudgetRepository(requireContext())
        val prefs = requireContext().getSharedPreferences("BudgetQuestPrefs", 0)
        userId = prefs.getInt("userId", -1)

        adapter = TransactionAdapter(emptyList())
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        // Observe all expenses
        repository.getAllExpenses(userId).observe(viewLifecycleOwner) { expenses ->
            allExpenses = expenses
            applyFilters()
        }

        // Filter buttons
        binding.btnFilterAll.setOnClickListener {
            currentFilter = "ALL"
            updateFilterButtons()
            applyFilters()
        }

        binding.btnFilterIncome.setOnClickListener {
            currentFilter = "INCOME"
            updateFilterButtons()
            applyFilters()
        }

        binding.btnFilterExpense.setOnClickListener {
            currentFilter = "EXPENSE"
            updateFilterButtons()
            applyFilters()
        }

        // Date pickers
        binding.btnStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    startDateFilter = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.btnStartDate.text = startDateFilter
                    applyFilters()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    endDateFilter = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.btnEndDate.text = endDateFilter
                    applyFilters()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnAddExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }
    }

    private fun applyFilters() {
        var filtered = allExpenses

        // Apply type filter
        filtered = when (currentFilter) {
            "INCOME" -> filtered.filter { it.isIncome }
            "EXPENSE" -> filtered.filter { !it.isIncome }
            else -> filtered
        }

        // Apply date filter
        if (startDateFilter.isNotEmpty()) {
            filtered = filtered.filter { it.date >= startDateFilter }
        }
        if (endDateFilter.isNotEmpty()) {
            filtered = filtered.filter { it.date <= endDateFilter }
        }

        adapter.updateExpenses(filtered)
        binding.tvNoTransactions.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateFilterButtons() {
        val activeColor = requireContext().getColor(com.example.budgetquest.R.color.teal_primary)
        val inactiveColor = requireContext().getColor(com.example.budgetquest.R.color.surface)
        val activeText = requireContext().getColor(com.example.budgetquest.R.color.background)
        val inactiveTextAll = requireContext().getColor(com.example.budgetquest.R.color.text_primary)
        val incomeColor = requireContext().getColor(com.example.budgetquest.R.color.income_green)
        val expenseColor = requireContext().getColor(com.example.budgetquest.R.color.expense_red)

        binding.btnFilterAll.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (currentFilter == "ALL") activeColor else inactiveColor)
        binding.btnFilterAll.setTextColor(
            if (currentFilter == "ALL") activeText else inactiveTextAll)

        binding.btnFilterIncome.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (currentFilter == "INCOME") activeColor else inactiveColor)
        binding.btnFilterIncome.setTextColor(
            if (currentFilter == "INCOME") activeText else incomeColor)

        binding.btnFilterExpense.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (currentFilter == "EXPENSE") activeColor else inactiveColor)
        binding.btnFilterExpense.setTextColor(
            if (currentFilter == "EXPENSE") activeText else expenseColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}