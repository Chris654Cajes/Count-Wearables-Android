package com.countwearables.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.countwearables.app.R
import com.countwearables.app.databinding.ActivityRegisterBinding
import com.countwearables.app.ui.viewmodel.AuthViewModel

/**
 * Register Activity - Allows new users to create an account.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { result ->
            result.onSuccess { user ->
                Toast.makeText(this, R.string.registration_successful, Toast.LENGTH_SHORT).show()
                navigateToMain()
            }.onFailure { error ->
                showError(error.message ?: "Registration failed")
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val confirmPassword = binding.editConfirmPassword.text.toString().trim()

            if (validateInput(username, password, confirmPassword)) {
                viewModel.register(username, password, confirmPassword)
            }
        }

        binding.textLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(username: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            binding.editUsername.error = getString(R.string.field_required)
            binding.editUsername.requestFocus()
            return false
        }

        if (username.length < 3) {
            binding.editUsername.error = getString(R.string.username_short)
            binding.editUsername.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.editPassword.error = getString(R.string.field_required)
            binding.editPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.editPassword.error = getString(R.string.password_short)
            binding.editPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.editConfirmPassword.error = getString(R.string.field_required)
            binding.editConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            binding.editConfirmPassword.error = getString(R.string.passwords_mismatch)
            binding.editConfirmPassword.requestFocus()
            return false
        }

        binding.editUsername.error = null
        binding.editPassword.error = null
        binding.editConfirmPassword.error = null
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.editUsername.isEnabled = !isLoading
        binding.editPassword.isEnabled = !isLoading
        binding.editConfirmPassword.isEnabled = !isLoading
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