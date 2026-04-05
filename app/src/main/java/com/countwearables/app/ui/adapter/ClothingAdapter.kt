package com.countwearables.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.countwearables.app.R
import com.countwearables.app.data.model.ClothingItem
import java.io.File

/**
 * RecyclerView Adapter for displaying clothing items in a list.
 * Uses ListAdapter with DiffUtil for efficient list updates.
 */
class ClothingAdapter(
    private val onItemClick: (ClothingItem) -> Unit
) : ListAdapter<ClothingItem, ClothingAdapter.ClothingViewHolder>(ClothingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clothing, parent, false)
        return ClothingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClothingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivItem: ImageView = itemView.findViewById(R.id.ivItem)
        private val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvSize: TextView = itemView.findViewById(R.id.tvSize)
        private val tvColor: TextView = itemView.findViewById(R.id.tvColor)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)

        fun bind(item: ClothingItem) {
            tvItemName.text = item.name
            tvCategory.text = item.category
            
            // Display size if available
            tvSize.text = if (item.size.isNotEmpty()) {
                itemView.context.getString(R.string.size) + ": " + item.size
            } else {
                ""
            }
            
            // Display color if available
            tvColor.text = if (item.color.isNotEmpty()) {
                item.color
            } else {
                ""
            }
            
            tvQuantity.text = itemView.context.getString(R.string.quantity) + ": " + item.quantity

            // Load image if available
            if (item.imagePath.isNotEmpty()) {
                val imageFile = File(item.imagePath)
                if (imageFile.exists()) {
                    Glide.with(itemView.context)
                        .load(imageFile)
                        .centerCrop()
                        .placeholder(R.drawable.ic_clothing_placeholder)
                        .error(R.drawable.ic_clothing_placeholder)
                        .into(ivItem)
                } else {
                    ivItem.setImageResource(R.drawable.ic_clothing_placeholder)
                }
            } else {
                ivItem.setImageResource(R.drawable.ic_clothing_placeholder)
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class ClothingDiffCallback : DiffUtil.ItemCallback<ClothingItem>() {
        override fun areItemsTheSame(oldItem: ClothingItem, newItem: ClothingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClothingItem, newItem: ClothingItem): Boolean {
            return oldItem == newItem
        }
    }
}