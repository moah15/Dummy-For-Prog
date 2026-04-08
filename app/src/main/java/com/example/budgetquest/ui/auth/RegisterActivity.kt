package com.example.budgetquest.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetquest.data.database.User
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: BudgetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = BudgetRepository(this)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.etUsername.error = "Please enter a username"
                return@setOnClickListener
            }
            if (username.length < 3) {
                binding.etUsername.error = "Username must be at least 3 characters"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Please enter a password"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }
            if (password != confirm) {
                binding.etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val existing = repository.getUserByUsername(username)
                if (existing != null) {
                    runOnUiThread {
                        binding.etUsername.error = "Username already taken"
                    }
                    return@launch
                }

                val newUser = User(
                    username = username,
                    password = password
                )
                repository.register(newUser)

                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created! Please login.",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
}