package com.countwearables.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlist_items",
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
data class WishlistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val name: String,
    val brand: String = "",
    val desiredPrice: Double? = null,
    val priority: Int = 1, // 1-Low, 2-Medium, 3-High
    val store: String = "",
    val notes: String = "",
    val imageUrl: String = "",
    val isPurchased: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
