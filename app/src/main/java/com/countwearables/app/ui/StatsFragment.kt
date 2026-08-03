package com.countwearables.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.countwearables.app.R
import com.countwearables.app.databinding.FragmentStatsBinding
import com.countwearables.app.ui.viewmodel.StatsViewModel
import java.util.Locale

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.totalValue.observe(viewLifecycleOwner) { value ->
            binding.tvTotalValue.text = String.format(Locale.getDefault(), "$%.2f", value ?: 0.0)
        }

        viewModel.mostExpensive.observe(viewLifecycleOwner) { item ->
            binding.tvMostExpensive.text = item?.name ?: "N/A"
        }

        viewModel.oldestItem.observe(viewLifecycleOwner) { item ->
            binding.tvOldestItem.text = item?.name ?: "N/A"
        }

        viewModel.categoryDistribution.observe(viewLifecycleOwner) { dist ->
            binding.layoutCategories.removeAllViews()
            dist.forEach { catCount ->
                val tv = TextView(requireContext())
                tv.text = "${catCount.category}: ${catCount.count}"
                tv.setTextColor(resources.getColor(R.color.text_primary, null))
                tv.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 4)
                }
                binding.layoutCategories.addView(tv)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
