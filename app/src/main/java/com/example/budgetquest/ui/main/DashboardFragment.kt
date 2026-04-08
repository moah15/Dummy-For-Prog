package com.example.budgetquest.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: BudgetRepository
    private var userId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = BudgetRepository(requireContext())
        val prefs = requireContext().getSharedPreferences("BudgetQuestPrefs", 0)
        userId = prefs.getInt("userId", -1)

        loadUserData()
        loadFinancialData()
        loadChart()

        binding.btnAddTransaction.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = repository.getUserById(userId) ?: return@launch
            requireActivity().runOnUiThread {
                binding.tvUsername.text = user.username
                binding.tvLevel.text = "Level ${user.level}"
                binding.tvStreak.text = "${user.streak} Day Streak"
            }
        }
    }

    private fun loadFinancialData() {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        lifecycleScope.launch {
            val income = repository.getTotalIncome(userId) ?: 0.0
            val expenses = repository.getTotalExpenses(userId) ?: 0.0
            val balance = income - expenses
            requireActivity().runOnUiThread {
                binding.tvTotalBalance.text = format.format(balance)
                binding.tvTotalIncome.text = format.format(income)
                binding.tvTotalExpenses.text = format.format(expenses)
            }
        }
    }

    private fun loadChart() {
        lifecycleScope.launch {
            // Get current month date range
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = SimpleDateFormat("yyyy-MM-01", Locale.getDefault()).format(Date())
            val endDate = sdf.format(Date())

            val totals = repository.getTotalPerCategory(userId, startDate, endDate)
            val categories = repository.getCategoriesOnce(userId)

            if (totals.isEmpty()) return@launch

            val entries = totals.mapNotNull { total ->
                val category = categories.find { it.id == total.categoryId }
                if (category != null && total.total > 0) {
                    PieEntry(total.total.toFloat(), category.name)
                } else null
            }

            if (entries.isEmpty()) return@launch

            requireActivity().runOnUiThread {
                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(
                        Color.parseColor("#00D4AA"),
                        Color.parseColor("#E74C3C"),
                        Color.parseColor("#F39C12"),
                        Color.parseColor("#9B59B6"),
                        Color.parseColor("#3498DB"),
                        Color.parseColor("#2ECC71"),
                        Color.parseColor("#E67E22"),
                        Color.parseColor("#1ABC9C")
                    )
                    valueTextColor = Color.WHITE
                    valueTextSize = 11f
                }

                binding.pieChart.apply {
                    data = PieData(dataSet)
                    description.isEnabled = false
                    setUsePercentValues(true)
                    setHoleColor(Color.parseColor("#1E2230"))
                    holeRadius = 40f
                    legend.textColor = Color.WHITE
                    setEntryLabelColor(Color.WHITE)
                    animateY(800)
                    invalidate()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}