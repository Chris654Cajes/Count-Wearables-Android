package com.countwearables.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.countwearables.app.R
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.databinding.ActivityDetailBinding
import com.countwearables.app.ui.viewmodel.AuthViewModel
import com.countwearables.app.ui.viewmodel.ClothingViewModel
import java.io.File

/**
 * Detail Activity - Displays detailed information about a clothing item.
 * Allows users to edit or delete the item.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val clothingViewModel: ClothingViewModel by viewModels()

    private var currentItem: ClothingItem? = null

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        if (itemId == -1L) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupObservers()
        setupClickListeners()

        // Load item details
        clothingViewModel.getItemById(itemId)
    }

    private fun setupObservers() {
        clothingViewModel.currentItem.observe(this) { item ->
            item?.let {
                currentItem = it
                displayItemDetails(it)
            }
        }

        clothingViewModel.isLoading.observe(this) { isLoading ->
            // Could show a loading indicator
        }

        clothingViewModel.deleteResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            currentItem?.let { item ->
                val intent = Intent(this, AddEditItemActivity::class.java)
                intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.id)
                startActivity(intent)
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun displayItemDetails(item: ClothingItem) {
        binding.tvItemName.text = item.name
        binding.chipCategory.text = item.category
        binding.tvQuantity.text = item.quantity.toString()
        binding.tvColor.text = item.color.ifEmpty { "N/A" }
        binding.tvSize.text = item.size.ifEmpty { "N/A" }
        binding.tvDateAdded.text = item.getFormattedDate()
        binding.tvNotes.text = item.notes.ifEmpty { "No notes" }

        // Show/hide notes card
        binding.cardNotes.visibility = if (item.notes.isNotEmpty()) View.VISIBLE else View.GONE

        // Load image
        if (item.imagePath.isNotEmpty()) {
            val imageFile = File(item.imagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_clothing_placeholder)
                    .error(R.drawable.ic_clothing_placeholder)
                    .into(binding.ivItem)
            } else {
                binding.ivItem.setImageResource(R.drawable.ic_clothing_placeholder)
            }
        } else {
            binding.ivItem.setImageResource(R.drawable.ic_clothing_placeholder)
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                currentItem?.let { item ->
                    val userId = authViewModel.getCurrentUserId()
                    clothingViewModel.deleteItem(item.id, userId)
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Reload item details in case they were modified
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        if (itemId != -1L) {
            clothingViewModel.getItemById(itemId)
        }
    }
}