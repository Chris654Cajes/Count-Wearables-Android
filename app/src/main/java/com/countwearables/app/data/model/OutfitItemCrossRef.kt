package com.countwearables.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "outfit_item_cross_ref",
    primaryKeys = ["outfitId", "clothingItemId"],
    foreignKeys = [
        ForeignKey(
            entity = Outfit::class,
            parentColumns = ["id"],
            childColumns = ["outfitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClothingItem::class,
            parentColumns = ["id"],
            childColumns = ["clothingItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["outfitId"]),
        Index(value = ["clothingItemId"])
    ]
)
data class OutfitItemCrossRef(
    val outfitId: Long,
    val clothingItemId: Long
)
