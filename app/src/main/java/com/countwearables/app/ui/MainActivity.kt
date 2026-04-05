package com.countwearables.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.countwearables.app.R
import com.countwearables.app.databinding.ActivityMainBinding
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.ui.adapter.ClothingAdapter
import com.countwearables.app.ui.viewmodel.AuthViewModel
import com.countwearables.app.ui.viewmodel.ClothingViewModel
import kotlinx.coroutines.launch

/**
 * Main Activity (Home Screen) - Displays list of clothing items.
 * Handles search, filter, and navigation to add/edit/detail screens.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val clothingViewModel: ClothingViewModel by viewModels()
    private lateinit var adapter: ClothingAdapter
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user ID from intent or auth repository
        userId = intent.getLongExtra("USER_ID", -1L)
        if (userId == -1L) {
            // Try to get from saved session
            userId = authViewModel.getCurrentUserId()
            
            if (userId == -1L) {
                // No user logged in, redirect to login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // Load items
        clothingViewModel.loadAllItems(userId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupRecyclerView() {
        adapter = ClothingAdapter { item ->
            // Open detail screen
            openDetailScreen(item)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        clothingViewModel.clothingItems.observe(this) { items ->
            updateUI(items)
        }

        clothingViewModel.isLoading.observe(this) { isLoading ->
            binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        clothingViewModel.itemResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show()
                clothingViewModel.loadAllItems(userId)
            }.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }

        clothingViewModel.deleteResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show()
                clothingViewModel.loadAllItems(userId)
            }.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddItem.setOnClickListener {
            openAddItemScreen()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clothingViewModel.searchItems(userId, s?.toString() ?: "")
            }
        })

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun updateUI(items: List<ClothingItem>) {
        adapter.submitList(items)
        
        if (items.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }

        // Update item count
        binding.tvItemCount.text = items.size.toString()

        // Update total quantity
        lifecycleScope.launch {
            val totalQty = clothingViewModel.getTotalQuantityForUser(userId)
            binding.tvTotalQuantity.text = totalQty.toString()
        }
    }

    private fun openAddItemScreen() {
        val intent = Intent(this, AddEditItemActivity::class.java)
        startActivity(intent)
    }

    private fun openDetailScreen(item: ClothingItem) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_ITEM_ID, item.id)
        startActivity(intent)
    }

    private fun showFilterDialog() {
        val categories = arrayOf("All", "Topwear", "Bottomwear", "Outerwear", "Footwear", "Accessories", "Sportswear", "Formal Wear", "Other")
        val sizes = arrayOf("All", "XS", "S", "M", "L", "XL", "XXL", "XXXL", "One Size", "Other")
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.filter)
        
        val items = arrayOf("Category", "Size", "Clear Filters")
        builder.setItems(items) { dialog, which ->
            when (which) {
                0 -> showCategoryFilter(categories)
                1 -> showSizeFilter(sizes)
                2 -> {
                    clothingViewModel.clearSearch(userId)
                    binding.editSearch.text?.clear()
                    Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.show()
    }

    private fun showCategoryFilter(categories: Array<String>) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Category")
        builder.setItems(categories) { dialog, which ->
            val category = categories[which]
            if (category == "All") {
                clothingViewModel.clearSearch(userId)
            } else {
                clothingViewModel.filterByCategory(userId, category)
            }
        }
        builder.show()
    }

    private fun showSizeFilter(sizes: Array<String>) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Size")
        builder.setItems(sizes) { dialog, which ->
            val size = sizes[which]
            if (size == "All") {
                clothingViewModel.clearSearch(userId)
            } else {
                clothingViewModel.filterBySize(userId, size)
            }
        }
        builder.show()
    }

    private fun logout() {
        authViewModel.logout()
        
        // Redirect to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from add/edit screen
        clothingViewModel.loadAllItems(userId)
    }
}