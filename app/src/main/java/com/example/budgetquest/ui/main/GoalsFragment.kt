package com.example.budgetquest.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.FragmentGoalsBinding

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: BudgetRepository
    private lateinit var adapter: GoalAdapter
    private var userId = -1

    companion object {
        private const val TAG = "GoalsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = BudgetRepository(requireContext())
        val prefs = requireContext().getSharedPreferences("BudgetQuestPrefs", 0)
        userId = prefs.getInt("userId", -1)

        Log.d(TAG, "GoalsFragment loaded for userId: $userId")

        // Setup RecyclerView
        adapter = GoalAdapter(emptyList())
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter

        // Observe goals — auto updates when new goal is added
        repository.getGoals(userId).observe(viewLifecycleOwner) { goals ->
            Log.d(TAG, "Goals updated: ${goals.size} goals found")
            adapter.updateGoals(goals)
            binding.tvNoGoals.visibility =
                if (goals.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.btnAddGoal.setOnClickListener {
            Log.d(TAG, "Add goal button clicked")
            startActivity(Intent(requireContext(), AddGoalActivity::class.java))
        }
    }

    // Refresh list every time we come back to this screen
    override fun onResume() {
        super.onResume()
        repository.getGoals(userId).observe(viewLifecycleOwner) { goals ->
            adapter.updateGoals(goals)
            binding.tvNoGoals.visibility =
                if (goals.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}