package com.ovijat.bakerystock.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ovijat.bakerystock.data.SeedData
import com.ovijat.bakerystock.data.dao.RmDao
import com.ovijat.bakerystock.data.entity.BatchLog
import com.ovijat.bakerystock.data.entity.Item
import com.ovijat.bakerystock.data.entity.Mixing2Usage
import com.ovijat.bakerystock.data.entity.StoreReceive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Item::class,
        StoreReceive::class,
        Mixing2Usage::class,
        BatchLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rmDao(): RmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ovijat_bakery_stock.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // ডাটাবেজ প্রথমবার তৈরি হলে ৩৩টি কাঁচামাল ব্যাকগ্রাউন্ড থ্রেডে যুক্ত হবে
        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        database.rmDao().insertAllItems(SeedData.initialItems)
                    }
                }
            }
        }
    }
}