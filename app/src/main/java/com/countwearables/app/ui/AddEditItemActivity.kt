package com.countwearables.app.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.countwearables.app.R
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.databinding.ActivityAddEditItemBinding
import com.countwearables.app.ui.viewmodel.AuthViewModel
import com.countwearables.app.ui.viewmodel.ClothingViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditItemBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val clothingViewModel: ClothingViewModel by viewModels()

    private var currentItem: ClothingItem? = null
    private var imageFilePath: String = ""
    private var selectedPurchaseDate: Long? = null

    private val imageCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            if (imageFilePath.isNotEmpty()) {
                Glide.with(this).load(File(imageFilePath)).into(binding.ivItem)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
        if (itemId != -1L) {
            clothingViewModel.setItemId(itemId)
        }

        setupObservers()
        setupClickListeners()
        setupDropdowns()
    }

    private fun setupObservers() {
        clothingViewModel.currentItem.observe(this) { item ->
            item?.let {
                currentItem = it
                populateFields(it)
            }
        }

        clothingViewModel.itemResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Saved to your inventory!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupDropdowns() {
        val categories = ClothingItem.DEFAULT_CATEGORIES.toTypedArray()
        binding.editCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories))

        val sizes = ClothingItem.DEFAULT_SIZES.toTypedArray()
        binding.editSize.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sizes))

        val seasons = ClothingItem.ALL_SEASONS.toTypedArray()
        binding.editSeason.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, seasons))
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener { validateAndSave() }
        binding.btnTakePhoto.setOnClickListener { checkCameraPermission() }
        binding.editPurchaseDate.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedCal = Calendar.getInstance()
            selectedCal.set(year, month, day)
            selectedPurchaseDate = selectedCal.timeInMillis
            binding.editPurchaseDate.setText(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedCal.time))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun populateFields(item: ClothingItem) {
        binding.editName.setText(item.name)
        binding.editBrand.setText(item.brand)
        binding.editCategory.setText(item.category, false)
        binding.editSize.setText(item.size, false)
        binding.editColor.setText(item.color)
        binding.editSeason.setText(item.season, false)
        binding.editPrice.setText(item.purchasePrice?.toString() ?: "")
        binding.editNotes.setText(item.notes)
        
        item.purchaseDate?.let {
            selectedPurchaseDate = it
            binding.editPurchaseDate.setText(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)))
        }

        imageFilePath = item.imagePath
        if (imageFilePath.isNotEmpty()) {
            Glide.with(this).load(File(imageFilePath)).into(binding.ivItem)
        }
    }

    private fun validateAndSave() {
        val name = binding.editName.text.toString().trim()
        val category = binding.editCategory.text.toString().trim()
        
        if (name.isEmpty()) {
            binding.textInputName.error = "Item name is required"
            return
        }

        val userId = authViewModel.getCurrentUserId()
        val item = (currentItem ?: ClothingItem(userId = userId)).copy(
            name = name,
            brand = binding.editBrand.text.toString().trim(),
            category = category,
            size = binding.editSize.text.toString().trim(),
            color = binding.editColor.text.toString().trim(),
            season = binding.editSeason.text.toString().trim(),
            purchasePrice = binding.editPrice.text.toString().toDoubleOrNull(),
            purchaseDate = selectedPurchaseDate,
            notes = binding.editNotes.text.toString().trim(),
            imagePath = imageFilePath
        )

        if (currentItem == null) clothingViewModel.addItem(item)
        else clothingViewModel.updateItem(item)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) launchCamera() }.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File(getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "IMG_${System.currentTimeMillis()}.jpg")
        imageFilePath = photoFile.absolutePath
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }
        imageCaptureLauncher.launch(intent)
    }

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }
}
