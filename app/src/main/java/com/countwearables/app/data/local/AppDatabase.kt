package com.countwearables.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.countwearables.app.data.local.dao.ClothingItemDao
import com.countwearables.app.data.local.dao.OutfitDao
import com.countwearables.app.data.local.dao.UserDao
import com.countwearables.app.data.local.dao.WishlistDao
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.data.model.Outfit
import com.countwearables.app.data.model.OutfitItemCrossRef
import com.countwearables.app.data.model.User
import com.countwearables.app.data.model.WishlistItem

@Database(
    entities = [
        User::class,
        ClothingItem::class,
        Outfit::class,
        OutfitItemCrossRef::class,
        WishlistItem::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun clothingItemDao(): ClothingItemDao
    abstract fun outfitDao(): OutfitDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        private const val DATABASE_NAME = "count_wearables.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                performFullMigration(db)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // If the user is on the "bad" version 2, we recreate tables to fix the schema
                performFullMigration(db)
            }
        }

        private fun performFullMigration(db: SupportSQLiteDatabase) {
            db.execSQL("PRAGMA foreign_keys=OFF")

            // 1. Migrate 'users' table
            db.execSQL("DROP TABLE IF EXISTS `users_new` ")
            db.execSQL("CREATE TABLE IF NOT EXISTS `users_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL, `password` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
            
            // Check if users has createdAt (it might from a bad v2)
            val usersCursor = db.query("PRAGMA table_info(users)")
            var hasCreatedAt = false
            while (usersCursor.moveToNext()) {
                if (usersCursor.getString(1) == "createdAt") {
                    hasCreatedAt = true
                    break
                }
            }
            usersCursor.close()

            if (hasCreatedAt) {
                db.execSQL("INSERT INTO `users_new` (`id`, `username`, `password`, `createdAt`) SELECT `id`, `username`, `password`, `createdAt` FROM `users` ")
            } else {
                db.execSQL("INSERT INTO `users_new` (`id`, `username`, `password`, `createdAt`) SELECT `id`, `username`, `password`, ${System.currentTimeMillis()} FROM `users` ")
            }
            db.execSQL("DROP TABLE `users` ")
            db.execSQL("ALTER TABLE `users_new` RENAME TO `users` ")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_username` ON `users` (`username`)")

            // 2. Migrate 'clothes' table
            db.execSQL("DROP TABLE IF EXISTS `clothes_new` ")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `clothes_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `userId` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `category` TEXT NOT NULL, 
                    `quantity` INTEGER NOT NULL, 
                    `color` TEXT NOT NULL, 
                    `size` TEXT NOT NULL, 
                    `notes` TEXT NOT NULL, 
                    `imagePath` TEXT NOT NULL, 
                    `dateAdded` INTEGER NOT NULL, 
                    `lastWornDate` INTEGER, 
                    `wearCount` INTEGER NOT NULL, 
                    `purchasePrice` REAL, 
                    `purchaseDate` INTEGER, 
                    `brand` TEXT NOT NULL, 
                    `store` TEXT NOT NULL, 
                    `laundryStatus` TEXT NOT NULL, 
                    `season` TEXT NOT NULL, 
                    `isFavorite` INTEGER NOT NULL, 
                    FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())

            // Map old columns to new ones. Handle v1 (snake_case) or v2 (partially migrated)
            val clothesCursor = db.query("PRAGMA table_info(clothes)")
            val columns = mutableListOf<String>()
            while (clothesCursor.moveToNext()) {
                columns.add(clothesCursor.getString(1))
            }
            clothesCursor.close()

            val userIdCol = if (columns.contains("userId")) "userId" else "user_id"
            val imagePathCol = if (columns.contains("imagePath")) "imagePath" else "image_path"
            val dateAddedCol = if (columns.contains("dateAdded")) "dateAdded" else "date_added"

            db.execSQL("""
                INSERT INTO `clothes_new` (
                    `id`, `userId`, `name`, `category`, `quantity`, `color`, `size`, `notes`, `imagePath`, `dateAdded`, 
                    `lastWornDate`, `wearCount`, `purchasePrice`, `purchaseDate`, `brand`, `store`, `laundryStatus`, `season`, `isFavorite`
                ) SELECT 
                    `id`, `$userIdCol`, `name`, `category`, `quantity`, 
                    COALESCE(`color`, ''), 
                    COALESCE(`size`, ''), 
                    COALESCE(`notes`, ''), 
                    COALESCE(`$imagePathCol`, ''), 
                    `$dateAddedCol`,
                    ${if (columns.contains("lastWornDate")) "`lastWornDate`" else "NULL"},
                    ${if (columns.contains("wearCount")) "`wearCount`" else "0"},
                    ${if (columns.contains("purchasePrice")) "`purchasePrice`" else "NULL"},
                    ${if (columns.contains("purchaseDate")) "`purchaseDate`" else "NULL"},
                    ${if (columns.contains("brand")) "`brand`" else "''"},
                    ${if (columns.contains("store")) "`store`" else "''"},
                    ${if (columns.contains("laundryStatus")) "`laundryStatus`" else "'Ready'"},
                    ${if (columns.contains("season")) "`season`" else "'All Season'"},
                    ${if (columns.contains("isFavorite")) "`isFavorite`" else "0"}
                FROM `clothes`
            """.trimIndent())

            db.execSQL("DROP TABLE `clothes` ")
            db.execSQL("ALTER TABLE `clothes_new` RENAME TO `clothes` ")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_clothes_userId` ON `clothes` (`userId`)")

            // 3. Create/Fix other tables
            db.execSQL("DROP TABLE IF EXISTS `outfits` ")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `outfits` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `userId` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `description` TEXT NOT NULL, 
                    `occasion` TEXT NOT NULL, 
                    `season` TEXT NOT NULL, 
                    `isFavorite` INTEGER NOT NULL, 
                    `thumbnailPath` TEXT, 
                    `createdAt` INTEGER NOT NULL, 
                    FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_outfits_userId` ON `outfits` (`userId`)")

            db.execSQL("DROP TABLE IF EXISTS `outfit_item_cross_ref` ")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `outfit_item_cross_ref` (
                    `outfitId` INTEGER NOT NULL, 
                    `clothingItemId` INTEGER NOT NULL, 
                    PRIMARY KEY(`outfitId`, `clothingItemId`), 
                    FOREIGN KEY(`outfitId`) REFERENCES `outfits`(`id`) ON DELETE CASCADE, 
                    FOREIGN KEY(`clothingItemId`) REFERENCES `clothes`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_outfit_item_cross_ref_outfitId` ON `outfit_item_cross_ref` (`outfitId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_outfit_item_cross_ref_clothingItemId` ON `outfit_item_cross_ref` (`clothingItemId`)")

            db.execSQL("DROP TABLE IF EXISTS `wishlist_items` ")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `wishlist_items` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `userId` INTEGER NOT NULL, 
                    `name` TEXT NOT NULL, 
                    `brand` TEXT NOT NULL, 
                    `desiredPrice` REAL, 
                    `priority` INTEGER NOT NULL, 
                    `store` TEXT NOT NULL, 
                    `notes` TEXT NOT NULL, 
                    `imageUrl` TEXT NOT NULL, 
                    `isPurchased` INTEGER NOT NULL, 
                    `dateAdded` INTEGER NOT NULL, 
                    FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_wishlist_items_userId` ON `wishlist_items` (`userId`)")

            db.execSQL("PRAGMA foreign_keys=ON")
        }
    }
}
