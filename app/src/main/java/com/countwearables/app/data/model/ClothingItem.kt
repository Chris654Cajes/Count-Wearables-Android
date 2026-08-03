package com.countwearables.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a clothing item in the user's inventory.
 * Each item is associated with a user through the userId foreign key.
 */
@Entity(
    tableName = "clothes",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class ClothingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val name: String = "",
    val category: String = "",
    val quantity: Int = 1,
    val color: String = "",
    val size: String = "",
    val notes: String = "",
    val imagePath: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val lastWornDate: Long? = null,
    val wearCount: Int = 0,
    val purchasePrice: Double? = null,
    val purchaseDate: Long? = null,
    val brand: String = "",
    val store: String = "",
    val laundryStatus: String = LAUNDRY_READY,
    val season: String = SEASON_ALL,
    val isFavorite: Boolean = false
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(dateAdded))
    }

    fun getFormattedLastWornDate(): String {
        if (lastWornDate == null) return "Never worn"
        val diff = System.currentTimeMillis() - lastWornDate
        val days = diff / (1000 * 60 * 60 * 24)
        return when {
            days == 0L -> "Worn today"
            days == 1L -> "Worn yesterday"
            days < 7L -> "Worn $days days ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "Last worn ${sdf.format(Date(lastWornDate))}"
            }
        }
    }

    fun getCostPerWear(): Double {
        if (purchasePrice == null || wearCount == 0) return purchasePrice ?: 0.0
        return purchasePrice / wearCount
    }
    
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               category.isNotBlank() && 
               quantity > 0
    }
    
    fun withQuantity(newQuantity: Int): ClothingItem {
        return copy(quantity = newQuantity)
    }
    
    fun withImage(newPath: String): ClothingItem {
        return copy(imagePath = newPath)
    }
    
    companion object {
        const val LAUNDRY_CLEAN = "Clean"
        const val LAUNDRY_DIRTY = "Dirty"
        const val LAUNDRY_WASHING = "Washing"
        const val LAUNDRY_DRYING = "Drying"
        const val LAUNDRY_READY = "Ready"

        val ALL_LAUNDRY_STATUSES = listOf(
            LAUNDRY_CLEAN, LAUNDRY_DIRTY, LAUNDRY_WASHING, LAUNDRY_DRYING, LAUNDRY_READY
        )

        const val SEASON_SPRING = "Spring"
        const val SEASON_SUMMER = "Summer"
        const val SEASON_AUTUMN = "Autumn"
        const val SEASON_WINTER = "Winter"
        const val SEASON_ALL = "All Season"

        val ALL_SEASONS = listOf(
            SEASON_SPRING, SEASON_SUMMER, SEASON_AUTUMN, SEASON_WINTER, SEASON_ALL
        )

        val DEFAULT_CATEGORIES = listOf(
            "Topwear", "Bottomwear", "Outerwear", "Footwear",
            "Accessories", "Underwear", "Sportswear", "Formal Wear", "Other"
        )
        
        val DEFAULT_SIZES = listOf(
            "XS", "S", "M", "L", "XL", "XXL", "XXXL", "One Size", "Other"
        )
    }
}
