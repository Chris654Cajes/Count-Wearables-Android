package com.countwearables.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.countwearables.app.databinding.FragmentWardrobeBinding
import com.countwearables.app.ui.adapter.ClothingAdapter
import com.countwearables.app.ui.viewmodel.AuthViewModel
import com.countwearables.app.ui.viewmodel.ClothingViewModel

class WardrobeFragment : Fragment() {

    private var _binding: FragmentWardrobeBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by viewModels()
    private val clothingViewModel: ClothingViewModel by viewModels()
    private lateinit var adapter: ClothingAdapter
    private var userId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userId = authViewModel.getCurrentUserId()
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        clothingViewModel.loadAllItems(userId)
    }

    private fun setupRecyclerView() {
        adapter = ClothingAdapter { item ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_ITEM_ID, item.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        clothingViewModel.clothingItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.fabAddItem.setOnClickListener {
            val intent = Intent(requireContext(), AddEditItemActivity::class.java)
            startActivity(intent)
        }

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clothingViewModel.searchItems(userId, s?.toString() ?: "")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        clothingViewModel.loadAllItems(userId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
