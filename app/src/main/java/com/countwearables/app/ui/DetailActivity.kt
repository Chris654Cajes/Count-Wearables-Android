package com.countwearables.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.countwearables.app.R
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.databinding.ActivityDetailBinding
import com.countwearables.app.ui.util.CategoryStyle
import com.countwearables.app.ui.viewmodel.ClothingViewModel
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
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
            finish()
            return
        }

        clothingViewModel.setItemId(itemId)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        clothingViewModel.currentItem.observe(this) { item ->
            item?.let {
                currentItem = it
                displayItemDetails(it)
            }
        }

        clothingViewModel.deleteResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show()
                finish()
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

        binding.btnMarkWorn.setOnClickListener {
            currentItem?.let { clothingViewModel.markAsWorn(it.id) }
        }

        binding.btnFavorite.setOnClickListener {
            currentItem?.let { clothingViewModel.toggleFavorite(it.id) }
        }

        binding.chipLaundryStatus.setOnClickListener {
            showLaundryStatusDialog()
        }
    }

    private fun displayItemDetails(item: ClothingItem) {
        binding.tvItemName.text = item.name
        binding.chipCategory.text = item.category
        binding.chipCategory.setChipBackgroundColorResource(CategoryStyle.colorRes(item.category))
        binding.tvSize.text = item.size.ifEmpty { "N/A" }
        binding.tvBrand.text = item.brand.ifEmpty { "Unbranded" }
        binding.tvSeason.text = item.season
        
        binding.tvWearCount.text = item.wearCount.toString()
        binding.tvLastWorn.text = item.getFormattedLastWornDate()
        binding.tvCostPerWear.text = String.format("$%.2f", item.getCostPerWear())

        binding.chipLaundryStatus.text = item.laundryStatus
        
        val favIcon = if (item.isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        binding.btnFavorite.setImageResource(favIcon)

        if (item.imagePath.isNotEmpty()) {
            val imageFile = File(item.imagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_clothing_placeholder)
                    .into(binding.ivItem)
            }
        }
    }

    private fun showLaundryStatusDialog() {
        val statuses = ClothingItem.ALL_LAUNDRY_STATUSES.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Update Laundry Status")
            .setItems(statuses) { _, which ->
                currentItem?.let { clothingViewModel.updateLaundryStatus(it.id, statuses[which]) }
            }
            .show()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                currentItem?.let { clothingViewModel.deleteItem(it) }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
