package com.countwearables.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.countwearables.app.data.model.ClothingItem
import com.countwearables.app.data.model.User

/**
 * SQLite database helper class that manages database creation and version management.
 */
class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "count_wearables.db"
        private const val DATABASE_VERSION = 1
        
        const val TABLE_USERS = "users"
        const val TABLE_CLOTHES = "clothes"
        
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        
        const val COLUMN_CLOTHES_ID = "id"
        const val COLUMN_USER_ID_FK = "user_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_COLOR = "color"
        const val COLUMN_SIZE = "size"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_DATE_ADDED = "date_added"
        
        private const val TAG = "AppDatabase"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val CREATE_USERS_TABLE = """
        CREATE TABLE $TABLE_USERS (
            $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
            $COLUMN_PASSWORD TEXT NOT NULL
        )
    """.trimIndent()

    private val CREATE_CLOTHES_TABLE = """
        CREATE TABLE $TABLE_CLOTHES (
            $COLUMN_CLOTHES_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID_FK INTEGER NOT NULL,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_CATEGORY TEXT NOT NULL,
            $COLUMN_QUANTITY INTEGER NOT NULL DEFAULT 1,
            $COLUMN_COLOR TEXT,
            $COLUMN_SIZE TEXT,
            $COLUMN_NOTES TEXT,
            $COLUMN_IMAGE_PATH TEXT,
            $COLUMN_DATE_ADDED INTEGER NOT NULL,
            FOREIGN KEY ($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_CLOTHES_TABLE)
        Log.d(TAG, "Database created successfully")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLOTHES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // ==================== USER OPERATIONS ====================

    fun insertUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_PASSWORD, user.password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun validateCredentials(username: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_PASSWORD),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null, "1"
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
        }
        cursor.close()
        return user
    }

    fun isUsernameTaken(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null, "1"
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserById(userId: Long): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_PASSWORD),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null, "1"
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
        }
        cursor.close()
        return user
    }

    // ==================== CLOTHING ITEM OPERATIONS ====================

    fun insertClothingItem(item: ClothingItem): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID_FK, item.userId)
            put(COLUMN_NAME, item.name)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_COLOR, item.color)
            put(COLUMN_SIZE, item.size)
            put(COLUMN_NOTES, item.notes)
            put(COLUMN_IMAGE_PATH, item.imagePath)
            put(COLUMN_DATE_ADDED, item.dateAdded)
        }
        return db.insert(TABLE_CLOTHES, null, values)
    }

    fun updateClothingItem(item: ClothingItem): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, item.name)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_COLOR, item.color)
            put(COLUMN_SIZE, item.size)
            put(COLUMN_NOTES, item.notes)
            put(COLUMN_IMAGE_PATH, item.imagePath)
        }
        return db.update(TABLE_CLOTHES, values, "$COLUMN_CLOTHES_ID = ?", arrayOf(item.id.toString()))
    }

    fun deleteClothingItem(itemId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_CLOTHES, "$COLUMN_CLOTHES_ID = ?", arrayOf(itemId.toString()))
    }

    fun getClothingItemById(itemId: Long): ClothingItem? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CLOTHES,
            null,
            "$COLUMN_CLOTHES_ID = ?",
            arrayOf(itemId.toString()),
            null, null, null, "1"
        )
        var item: ClothingItem? = null
        if (cursor.moveToFirst()) {
            item = cursorToClothingItem(cursor)
        }
        cursor.close()
        return item
    }

    fun getAllClothingItemsForUser(userId: Long): List<ClothingItem> {
        val items = mutableListOf<ClothingItem>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CLOTHES,
            null,
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_DATE_ADDED DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToClothingItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun searchClothingItems(userId: Long, query: String): List<ClothingItem> {
        val items = mutableListOf<ClothingItem>()
        val db = this.readableDatabase
        val searchPattern = "%$query%"
        val cursor = db.query(
            TABLE_CLOTHES,
            null,
            "$COLUMN_USER_ID_FK = ? AND ($COLUMN_NAME LIKE ? OR $COLUMN_CATEGORY LIKE ?)",
            arrayOf(userId.toString(), searchPattern, searchPattern),
            null, null,
            "$COLUMN_DATE_ADDED DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToClothingItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun filterByCategory(userId: Long, category: String): List<ClothingItem> {
        val items = mutableListOf<ClothingItem>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CLOTHES,
            null,
            "$COLUMN_USER_ID_FK = ? AND $COLUMN_CATEGORY = ?",
            arrayOf(userId.toString(), category),
            null, null,
            "$COLUMN_DATE_ADDED DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToClothingItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun filterBySize(userId: Long, size: String): List<ClothingItem> {
        val items = mutableListOf<ClothingItem>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CLOTHES,
            null,
            "$COLUMN_USER_ID_FK = ? AND $COLUMN_SIZE = ?",
            arrayOf(userId.toString(), size),
            null, null,
            "$COLUMN_DATE_ADDED DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToClothingItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun filterByColor(userId: Long, color: String): List<ClothingItem> {
        val items = mutableListOf<ClothingItem>()
        val db = this.readableDatabase
        val searchPattern = "%$color%"
        val cursor = db.query(
            TABLE_CLOTHES,
            null,
            "$COLUMN_USER_ID_FK = ? AND $COLUMN_COLOR LIKE ?",
            arrayOf(userId.toString(), searchPattern),
            null, null,
            "$COLUMN_DATE_ADDED DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToClothingItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun getClothingItemCountForUser(userId: Long): Int {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CLOTHES,
            arrayOf("COUNT(*)"),
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getTotalQuantityForUser(userId: Long): Int {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CLOTHES,
            arrayOf("SUM($COLUMN_QUANTITY)"),
            "$COLUMN_USER_ID_FK = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        var total = 0
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    private fun cursorToClothingItem(cursor: Cursor): ClothingItem {
        return ClothingItem(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLOTHES_ID)),
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
            color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)) ?: "",
            size = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIZE)) ?: "",
            notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)) ?: "",
            imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)) ?: "",
            dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE_ADDED))
        )
    }
}