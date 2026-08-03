package com.countwearables.app.ui.util

import com.countwearables.app.R

/**
 * Maps a clothing category to a "gear slot" accent color, giving each
 * item card a rarity-style border/tag color like an RPG inventory screen.
 */
object CategoryStyle {

    fun colorRes(category: String): Int {
        return when (category.trim().lowercase()) {
            "topwear" -> R.color.category_topwear
            "bottomwear" -> R.color.category_bottomwear
            "outerwear" -> R.color.category_outerwear
            "footwear" -> R.color.category_footwear
            "accessories" -> R.color.category_accessories
            "underwear" -> R.color.category_sleepwear
            "sportswear" -> R.color.category_sportswear
            "formal wear", "dresses" -> R.color.category_dresses
            "sleepwear" -> R.color.category_sleepwear
            "swimwear" -> R.color.category_swimwear
            else -> R.color.category_other
        }
    }
}
