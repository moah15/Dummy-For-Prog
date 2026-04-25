package com.example.budgetquest.ui.main

// Android imports for date picker, intents, and UI components
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetquest.data.database.Category
import com.example.budgetquest.data.database.Expense
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.FragmentTransactionsBinding
import java.util.Calendar
import androidx.lifecycle.lifecycleScope //added this new import
/**
 * TransactionsFragment.kt
 * Displays all user transactions with filtering options.
 * Filters include: All/Income/Expense toggle, date range picker,
 * category filter and reset functionality.
 *
 * References:
 * - Android RecyclerView: https://developer.android.com/guide/topics/ui/layout/recyclerview
 * - Android DatePickerDialog: https://developer.android.com/reference/android/app/DatePickerDialog
 * - LiveData: https://developer.android.com/topic/libraries/architecture/livedata
 */
class TransactionsFragment : Fragment() {

    // View binding reference — automatically null when view is destroyed
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    // Repository handles all database operations
    private lateinit var repository: BudgetRepository

    // Adapter for the RecyclerView list
    private lateinit var adapter: TransactionAdapter


    private var userId = -1


    private var allExpenses = listOf<Expense>()


    private var allCategories = listOf<Category>()

    // Current active filters
    private var currentFilter = "ALL"       // ALL, INCOME, or EXPENSE
    private var startDateFilter = ""        // Format: yyyy-MM-dd
    private var endDateFilter = ""          // Format: yyyy-MM-dd
    private var selectedCategoryId = -1     // -1 means no category filter

    companion object {
        private const val TAG = "TransactionsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout using view binding
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize repository and get current user ID
        repository = BudgetRepository(requireContext())
        val prefs = requireContext().getSharedPreferences("BudgetQuestPrefs", 0)
        userId = prefs.getInt("userId", -1)
        Log.d(TAG, "TransactionsFragment loaded for userId: $userId")

        setupRecyclerView()
        setupFilterButtons()
        setupDatePickers()
        setupResetButton()
        loadCategories()
        observeTransactions()

        // Navigate to AddExpenseActivity when add button is clicked
        binding.btnAddExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and empty adapter.
     * The adapter is updated when transactions are loaded from the database.
     */
    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList())
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
        Log.d(TAG, "RecyclerView setup complete")
    }

    /**
     * Sets up the All/Income/Expense filter buttons.
     * Each button updates currentFilter and re-applies all active filters.
     */
    private fun setupFilterButtons() {
        binding.btnFilterAll.setOnClickListener {
            currentFilter = "ALL"
            updateFilterButtonColors()
            applyFilters()
            Log.d(TAG, "Filter set to ALL")
        }

        binding.btnFilterIncome.setOnClickListener {
            currentFilter = "INCOME"
            updateFilterButtonColors()
            applyFilters()
            Log.d(TAG, "Filter set to INCOME")
        }

        binding.btnFilterExpense.setOnClickListener {
            currentFilter = "EXPENSE"
            updateFilterButtonColors()
            applyFilters()
            Log.d(TAG, "Filter set to EXPENSE")
        }
    }

    /**

     * Reference: https://developer.android.com/reference/android/app/DatePickerDialog
     */
    private fun setupDatePickers() {
        // Start date picker
        binding.btnStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    startDateFilter = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.btnStartDate.text = "From: $startDateFilter"
                    Log.d(TAG, "Start date filter set: $startDateFilter")
                    applyFilters()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // End date picker
        binding.btnEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    endDateFilter = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.btnEndDate.text = "To: $endDateFilter"
                    Log.d(TAG, "End date filter set: $endDateFilter")
                    applyFilters()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    /**
     * Sets up the reset button to clear all active filters.
     * Resets date range and categories
     */
    private fun setupResetButton() {
        binding.btnReset.setOnClickListener {
            Log.d(TAG, "Resetting all filters")

            // Clear all filter values
            currentFilter = "ALL"
            startDateFilter = ""
            endDateFilter = ""
            selectedCategoryId = -1

            // Reset button labels
            binding.btnStartDate.text = "From Date"
            binding.btnEndDate.text = "To Date"

            // Reset category spinner to first item (All Categories)
            binding.spinnerCategory.setSelection(0)

            // Reset filter button colors
            updateFilterButtonColors()

            // Show all transactions
            applyFilters()

            Log.d(TAG, "All filters reset successfully")
        }
    }

    /**
     * Loads categories from the database and populates the category spinner.
     * First item is always "All Categories" which shows all transactions.
     * Uses coroutines via lifecycleScope for background database access.
     */
    private fun loadCategories() {
        // Use viewLifecycleOwner coroutine scope
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                allCategories = repository.getCategoriesOnce(userId)
                Log.d(TAG, "Loaded ${allCategories.size} categories")

                // Build spinner items — first is always "All Categories"
                val categoryNames = mutableListOf("All Categories")
                categoryNames.addAll(allCategories.map { it.name })

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )
                spinnerAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )

                requireActivity().runOnUiThread {
                    binding.spinnerCategory.adapter = spinnerAdapter

                    // Listen for category selection changes
                    binding.spinnerCategory.onItemSelectedListener =
                        object : android.widget.AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: android.widget.AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                // Position 0 = All Categories, positions 1+ = actual categories
                                selectedCategoryId = if (position == 0) -1
                                else allCategories[position - 1].id

                                Log.d(TAG, "Category filter: $selectedCategoryId")
                                applyFilters()
                            }

                            override fun onNothingSelected(
                                parent: android.widget.AdapterView<*>?
                            ) {}
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories: ${e.message}")
            }
        }
    }

    /**
     * Observes the LiveData list of all transactions for the current user.
     * Any database change automatically triggers a UI update.
     * Reference: https://developer.android.com/topic/libraries/architecture/livedata
     */
    private fun observeTransactions() {
        repository.getAllExpenses(userId).observe(viewLifecycleOwner) { expenses ->
            Log.d(TAG, "Transactions updated: ${expenses.size} total")
            allExpenses = expenses
            applyFilters()
        }
    }

    /**
     * Applies all currently active filters to the full transaction list.
     * Filters are applied in order: type → date range → category.
     * Updates the RecyclerView adapter with the filtered results.
     */
    private fun applyFilters() {
        var filtered = allExpenses

        // Step 1: Apply type filter (All / Income / Expense)
        filtered = when (currentFilter) {
            "INCOME" -> filtered.filter { it.isIncome }
            "EXPENSE" -> filtered.filter { !it.isIncome }
            else -> filtered
        }

        // Step 2: Apply start date filter if set
        if (startDateFilter.isNotEmpty()) {
            filtered = filtered.filter { it.date >= startDateFilter }
        }

        // Step 3: Apply end date filter if set
        if (endDateFilter.isNotEmpty()) {
            filtered = filtered.filter { it.date <= endDateFilter }
        }

        // Step 4: Apply category filter if a specific category is selected
        if (selectedCategoryId != -1) {
            filtered = filtered.filter { it.categoryId == selectedCategoryId }
        }

        Log.d(TAG, "After filters: ${filtered.size} transactions shown")

        // Update the RecyclerView adapter with filtered results
        adapter.updateExpenses(filtered)

        // Show empty state message if no results
        binding.tvNoTransactions.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * Updates the visual state of the filter buttons to show which is active.
     * Active button uses teal background, inactive uses input_background color.
     */
    private fun updateFilterButtonColors() {
        val activeColor = requireContext().getColor(
            com.example.budgetquest.R.color.teal_primary
        )
        val inactiveColor = requireContext().getColor(
            com.example.budgetquest.R.color.input_background
        )
        val activeText = requireContext().getColor(
            com.example.budgetquest.R.color.background
        )
        val incomeColor = requireContext().getColor(
            com.example.budgetquest.R.color.income_green
        )
        val expenseColor = requireContext().getColor(
            com.example.budgetquest.R.color.expense_red
        )
        val whiteColor = requireContext().getColor(
            com.example.budgetquest.R.color.white
        )

        // All button
        binding.btnFilterAll.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (currentFilter == "ALL") activeColor else inactiveColor
            )
        binding.btnFilterAll.setTextColor(
            if (currentFilter == "ALL") activeText else whiteColor
        )

        // Income button
        binding.btnFilterIncome.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (currentFilter == "INCOME") activeColor else inactiveColor
            )
        binding.btnFilterIncome.setTextColor(
            if (currentFilter == "INCOME") activeText else incomeColor
        )

        // Expense button
        binding.btnFilterExpense.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (currentFilter == "EXPENSE") activeColor else inactiveColor
            )
        binding.btnFilterExpense.setTextColor(
            if (currentFilter == "EXPENSE") activeText else expenseColor
        )
    }

    // Refresh list when returning to this screen
    override fun onResume() {
        super.onResume()
        observeTransactions()
        Log.d(TAG, "TransactionsFragment resumed")
    }

    // Clean up binding when view is destroyed to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}