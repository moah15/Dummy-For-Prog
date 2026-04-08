package com.example.budgetquest.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetquest.R
import com.example.budgetquest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load dashboard as the first screen
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        // Bottom navigation switching between fragments
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_dashboard -> DashboardFragment()
                R.id.navigation_transactions -> TransactionsFragment()
                R.id.navigation_goals -> GoalsFragment()
                R.id.navigation_achievements -> AchievementsFragment()
                else -> DashboardFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }
}