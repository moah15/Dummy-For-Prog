package com.example.budgetquest.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.FragmentAchievementsBinding
import kotlinx.coroutines.launch

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: BudgetRepository
    private var userId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = BudgetRepository(requireContext())
        val prefs = requireContext().getSharedPreferences("BudgetQuestPrefs", 0)
        userId = prefs.getInt("userId", -1)

        loadAchievements()
    }

    private fun loadAchievements() {
        lifecycleScope.launch {
            val user = repository.getUserById(userId) ?: return@launch
            requireActivity().runOnUiThread {
                binding.tvLevelValue.text = user.level.toString()
                binding.tvPoints.text = "${user.points} total points"
                binding.tvStreakValue.text = user.streak.toString()
                val progressPercent = (user.level - 1) * 12
                binding.tvProgressValue.text = "$progressPercent%"
                binding.tvAchievementProgress.text =
                    "${user.level - 1} of 8 unlocked ($progressPercent%)"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}