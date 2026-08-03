package com.countwearables.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an outfit composed of multiple clothing items.
 */
@Entity(
    tableName = "outfits",
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
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val name: String,
    val description: String = "",
    val occasion: String = "",
    val season: String = ClothingItem.SEASON_ALL,
    val isFavorite: Boolean = false,
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
