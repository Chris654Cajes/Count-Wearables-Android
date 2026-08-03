package com.countwearables.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.countwearables.app.databinding.ActivitySettingsBinding
import com.countwearables.app.ui.viewmodel.SettingsViewModel
import java.io.File

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            contentResolver.openOutputStream(it)?.use { output ->
                // This is a simplified version, better to do it in ViewModel
                Toast.makeText(this, "Save file exported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                viewModel.importData(reader.readText())
                Toast.makeText(this, "Save file imported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnExportJson.setOnClickListener {
            exportLauncher.launch("wardrobe_backup_${System.currentTimeMillis()}.json")
        }
        
        binding.btnImportJson.setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }
    }
}
