package com.countwearables.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.countwearables.app.databinding.FragmentDashboardBinding
import com.countwearables.app.ui.viewmodel.DashboardViewModel

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
    }
    
    private fun setupObservers() {
        viewModel.totalItems.observe(viewLifecycleOwner) { count ->
            binding.tvTotalItems.text = count.toString()
        }
        
        viewModel.laundryQueue.observe(viewLifecycleOwner) { queue ->
            binding.tvLaundryQueue.text = queue.size.toString()
        }
        
        viewModel.currentSeasonItems.observe(viewLifecycleOwner) { items ->
            binding.tvSeasonCount.text = "${items.size} Items available for current environment"
        }
        
        // Setup adapters for recently added and worn items...
        // For now, these are just placeholders
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
