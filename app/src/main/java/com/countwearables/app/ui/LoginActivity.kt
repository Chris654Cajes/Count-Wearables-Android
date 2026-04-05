package com.countwearables.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.countwearables.app.R
import com.countwearables.app.databinding.ActivityLoginBinding
import com.countwearables.app.ui.viewmodel.AuthViewModel

/**
 * Login Activity - Entry point for the app.
 * Handles user authentication and session management.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()

        // Check if user is already logged in
        if (viewModel.isLoggedIn()) {
            navigateToMain()
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                Toast.makeText(this, R.string.login_successful, Toast.LENGTH_SHORT).show()
                navigateToMain()
            }.onFailure { error ->
                showError(error.message ?: "Login failed")
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (validateInput(username, password)) {
                viewModel.login(username, password)
            }
        }

        binding.textRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.editUsername.error = getString(R.string.field_required)
            binding.editUsername.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.editPassword.error = getString(R.string.field_required)
            binding.editPassword.requestFocus()
            return false
        }

        binding.editUsername.error = null
        binding.editPassword.error = null
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.editUsername.isEnabled = !isLoading
        binding.editPassword.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.textError.text = message
        binding.textError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}