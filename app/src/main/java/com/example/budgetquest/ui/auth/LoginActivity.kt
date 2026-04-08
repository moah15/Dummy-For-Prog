package com.example.budgetquest.ui.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.ActivityLoginBinding
import com.example.budgetquest.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: BudgetRepository
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = BudgetRepository(this)
        prefs = getSharedPreferences("BudgetQuestPrefs", MODE_PRIVATE)

        // Clear any saved session so login is always required
        prefs.edit().remove("userId").apply()

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.etUsername.error = "Please enter your username"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = repository.login(username, password)
                if (user != null) {
                    prefs.edit().putInt("userId", user.id).apply()
                    runOnUiThread { goToMain() }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid username or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}